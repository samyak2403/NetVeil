#include <string.h>
#include <netdb.h>
#include <unistd.h>
#include <fcntl.h>

#include <jni.h>
#include <android/log.h>

#include "netveil/error.h"
#include "netveil/proxy.h"
#include "netveil/params.h"
#include "netveil/packets.h"
#include "main.h"
#include "utils.h"

const enum demode DESYNC_METHODS[] = {
    DESYNC_NONE,
    DESYNC_SPLIT,
    DESYNC_DISORDER,
    DESYNC_FAKE,
    DESYNC_OOB,
    DESYNC_DISOOB,
};

enum hosts_mode {
    HOSTS_DISABLE,
    HOSTS_BLACKLIST,
    HOSTS_WHITELIST,
};

JNIEXPORT jint JNI_OnLoad(
        __attribute__((unused)) JavaVM *vm,
        __attribute__((unused)) void *reserved) {
    default_params = params;
    return JNI_VERSION_1_6;
}

static jint internal_create_socket_cmd(JNIEnv *env, jobjectArray args) {
    reset_params();
    int argc = (*env)->GetArrayLength(env, args);
    char *argv[argc];
    for (int i = 0; i < argc; i++) {
        jstring arg = (jstring) (*env)->GetObjectArrayElement(env, args, i);
        const char *arg_str = (*env)->GetStringUTFChars(env, arg, 0);
        argv[i] = strdup(arg_str);
        (*env)->ReleaseStringUTFChars(env, arg, arg_str);
    }

    int res = parse_args(argc, argv);
    for (int i = 0; i < argc; i++) {
        free(argv[i]);
    }
    if (res < 0) {
        uniperror("parse_args");
        return -1;
    }

    int fd = listen_socket((struct sockaddr_ina *)&params.laddr);
    if (fd < 0) {
        uniperror("listen_socket");
        return -1;
    }
    LOG(LOG_S, "listen_socket, fd: %d", fd);

    return fd;
}

static jint internal_create_socket(
        JNIEnv *env,
        jstring ip,
        jint port,
        jint max_connections,
        jint buffer_size,
        jint default_ttl,
        jboolean custom_ttl,
        jboolean no_domain,
        jboolean desync_http,
        jboolean desync_https,
        jboolean desync_udp,
        jint desync_method,
        jint split_position,
        jboolean split_at_host,
        jint fake_ttl,
        jstring fake_sni,
        jbyte custom_oob_char,
        jboolean host_mixed_case,
        jboolean domain_mixed_case,
        jboolean host_remove_spaces,
        jboolean tls_record_split,
        jint tls_record_split_position,
        jboolean tls_record_split_at_sni,
        jint hosts_mode,
        jstring hosts,
        jboolean tfo,
        jint udp_fake_count,
        jboolean drop_sack,
        jint fake_offset) {
    reset_params();
    struct sockaddr_ina s;

    const char *address = (*env)->GetStringUTFChars(env, ip, 0);
    int res = get_addr(address, &s);
    (*env)->ReleaseStringUTFChars(env, ip, address);
    if (res < 0) {
        uniperror("get_addr");
        return -1;
    }

    s.in.sin_port = htons(port);

    params.max_open = max_connections;
    params.bfsize = buffer_size;
    params.resolve = !no_domain;
    params.tfo = tfo;

    if (custom_ttl) {
        params.def_ttl = default_ttl;
        params.custom_ttl = 1;
    }

    if (!params.def_ttl) {
        if ((params.def_ttl = get_default_ttl()) < 1) {
            uniperror("get_default_ttl");
            reset_params();
            return -1;
        }
    }

    if (hosts_mode == HOSTS_WHITELIST && hosts != NULL) {
        struct desync_params *dp = add(
                (void *) &params.dp,
                &params.dp_count,
                sizeof(struct desync_params)
        );
        if (!dp) {
            uniperror("add");
            reset_params();
            return -1;
        }

        const char *str = (*env)->GetStringUTFChars(env, hosts, 0);
        dp->file_ptr = data_from_str(str, &dp->file_size);
        (*env)->ReleaseStringUTFChars(env, hosts, str);
        dp->hosts = parse_hosts(dp->file_ptr, dp->file_size);
        if (!dp->hosts) {
            perror("parse_hosts");
            clear_params();
            return -1;
        }
    }

    struct desync_params *dp = add(
            (void *) &params.dp,
            &params.dp_count,
            sizeof(struct desync_params)
    );
    if (!dp) {
        uniperror("add");
        reset_params();
        return -1;
    }

    if (hosts_mode == HOSTS_BLACKLIST && hosts != NULL) {
        const char *str = (*env)->GetStringUTFChars(env, hosts, 0);
        dp->file_ptr = data_from_str(str, &dp->file_size);
        (*env)->ReleaseStringUTFChars(env, hosts, str);
        dp->hosts = parse_hosts(dp->file_ptr, dp->file_size);
        if (!dp->hosts) {
            perror("parse_hosts");
            clear_params();
            return -1;
        }
    }

    dp->ttl = fake_ttl;
    dp->udp_fake_count = udp_fake_count;
    dp->drop_sack = drop_sack;
    dp->proto =
            IS_HTTP * desync_http |
            IS_HTTPS * desync_https |
            IS_UDP * desync_udp;
    dp->mod_http =
            MH_HMIX * host_mixed_case |
            MH_DMIX * domain_mixed_case |
            MH_SPACE * host_remove_spaces;

    struct part *part = add(
            (void *) &dp->parts,
            &dp->parts_n,
            sizeof(struct part)
    );
    if (!part) {
        uniperror("add");
        reset_params();
        return -1;
    }

    enum demode mode = DESYNC_METHODS[desync_method];

    int offset_flag = dp->proto || desync_https ? OFFSET_SNI : OFFSET_HOST;

    part->flag = split_at_host ? offset_flag : 0;
    part->pos = split_position;
    part->m = mode;

    if (tls_record_split) {
        struct part *tlsrec_part = add(
                (void *) &dp->tlsrec,
                &dp->tlsrec_n,
                sizeof(struct part)
        );

        if (!tlsrec_part) {
            uniperror("add");
            reset_params();
            return -1;
        }

        tlsrec_part->flag = tls_record_split_at_sni ? offset_flag : 0;
        tlsrec_part->pos = tls_record_split_position;
    }

    if (mode == DESYNC_FAKE) {
        dp->fake_offset = fake_offset;

        const char *sni = (*env)->GetStringUTFChars(env, fake_sni, 0);
        LOG(LOG_S, "fake_sni: %s", sni);
        res = change_tls_sni(sni, fake_tls.data, fake_tls.size);
        (*env)->ReleaseStringUTFChars(env, fake_sni, sni);
        if (res) {
            fprintf(stderr, "error chsni\n");
            return -1;
        }
    }

    if (mode == DESYNC_OOB) {
        dp->oob_char[0] = custom_oob_char;
        dp->oob_char[1] = 1;
    }

    if (dp->proto) {
        dp = add((void *)&params.dp,
                 &params.dp_count, sizeof(struct desync_params));
        if (!dp) {
            uniperror("add");
            clear_params();
            return -1;
        }
    }

    params.mempool = mem_pool(0);
    if (!params.mempool) {
        uniperror("mem_pool");
        clear_params();
        return -1;
    }

    int fd = listen_socket(&s);
    if (fd < 0) {
        uniperror("listen_socket");
        return -1;
    }
    LOG(LOG_S, "listen_socket, fd: %d", fd);

    return fd;
}

static jint internal_start_proxy(jint fd) {
    LOG(LOG_S, "start_proxy, fd: %d", fd);
    NOT_EXIT = 1;
    if (event_loop(fd) < 0) {
        uniperror("event_loop");
        return get_e();
    }
    return 0;
}

static jint internal_stop_proxy(jint fd) {
    LOG(LOG_S, "stop_proxy, fd: %d", fd);

    NOT_EXIT = 0;

    struct sockaddr_ina s;
    socklen_t len = sizeof(s);
    if (getsockname(fd, &s.sa, &len) == 0) {
        int dummy = socket(s.sa.sa_family, SOCK_STREAM, 0);
        if (dummy >= 0) {
            #ifdef __linux__
            fcntl(dummy, F_SETFL, O_NONBLOCK);
            #endif
            connect(dummy, &s.sa, len);
            close(dummy);
        }
    }

    int res = shutdown(fd, SHUT_RDWR);
    close(fd);
    reset_params();

    if (res < 0) {
        uniperror("shutdown");
        return get_e();
    }
    return 0;
}

// -------------------------------------------------------------
// JNI Exports for com.samyak.netveil.core.NetVeilProxy
// -------------------------------------------------------------

JNIEXPORT jint JNICALL
Java_com_samyak_netveil_core_NetVeilProxy_jniCreateSocketWithCommandLine(
        JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jobjectArray args) {
    return internal_create_socket_cmd(env, args);
}

JNIEXPORT jint JNICALL
Java_com_samyak_netveil_core_NetVeilProxy_jniCreateSocket(
        JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jstring ip,
        jint port,
        jint max_connections,
        jint buffer_size,
        jint default_ttl,
        jboolean custom_ttl,
        jboolean no_domain,
        jboolean desync_http,
        jboolean desync_https,
        jboolean desync_udp,
        jint desync_method,
        jint split_position,
        jboolean split_at_host,
        jint fake_ttl,
        jstring fake_sni,
        jbyte custom_oob_char,
        jboolean host_mixed_case,
        jboolean domain_mixed_case,
        jboolean host_remove_spaces,
        jboolean tls_record_split,
        jint tls_record_split_position,
        jboolean tls_record_split_at_sni,
        jint hosts_mode,
        jstring hosts,
        jboolean tfo,
        jint udp_fake_count,
        jboolean drop_sack,
        jint fake_offset) {
    return internal_create_socket(
        env, ip, port, max_connections, buffer_size, default_ttl, custom_ttl,
        no_domain, desync_http, desync_https, desync_udp, desync_method,
        split_position, split_at_host, fake_ttl, fake_sni, custom_oob_char,
        host_mixed_case, domain_mixed_case, host_remove_spaces, tls_record_split,
        tls_record_split_position, tls_record_split_at_sni, hosts_mode, hosts,
        tfo, udp_fake_count, drop_sack, fake_offset
    );
}

JNIEXPORT jint JNICALL
Java_com_samyak_netveil_core_NetVeilProxy_jniStartProxy(
        __attribute__((unused)) JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jint fd) {
    return internal_start_proxy(fd);
}

JNIEXPORT jint JNICALL
Java_com_samyak_netveil_core_NetVeilProxy_jniStopProxy(
        __attribute__((unused)) JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jint fd) {
    return internal_stop_proxy(fd);
}

// -------------------------------------------------------------
// JNI Exports for com.samyak.netveil.core.ByeDpiProxy
// -------------------------------------------------------------

JNIEXPORT jint JNICALL
Java_com_samyak_netveil_core_ByeDpiProxy_jniCreateSocketWithCommandLine(
        JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jobjectArray args) {
    return internal_create_socket_cmd(env, args);
}

JNIEXPORT jint JNICALL
Java_com_samyak_netveil_core_ByeDpiProxy_jniCreateSocket(
        JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jstring ip,
        jint port,
        jint max_connections,
        jint buffer_size,
        jint default_ttl,
        jboolean custom_ttl,
        jboolean no_domain,
        jboolean desync_http,
        jboolean desync_https,
        jboolean desync_udp,
        jint desync_method,
        jint split_position,
        jboolean split_at_host,
        jint fake_ttl,
        jstring fake_sni,
        jbyte custom_oob_char,
        jboolean host_mixed_case,
        jboolean domain_mixed_case,
        jboolean host_remove_spaces,
        jboolean tls_record_split,
        jint tls_record_split_position,
        jboolean tls_record_split_at_sni,
        jint hosts_mode,
        jstring hosts,
        jboolean tfo,
        jint udp_fake_count,
        jboolean drop_sack,
        jint fake_offset) {
    return internal_create_socket(
        env, ip, port, max_connections, buffer_size, default_ttl, custom_ttl,
        no_domain, desync_http, desync_https, desync_udp, desync_method,
        split_position, split_at_host, fake_ttl, fake_sni, custom_oob_char,
        host_mixed_case, domain_mixed_case, host_remove_spaces, tls_record_split,
        tls_record_split_position, tls_record_split_at_sni, hosts_mode, hosts,
        tfo, udp_fake_count, drop_sack, fake_offset
    );
}

JNIEXPORT jint JNICALL
Java_com_samyak_netveil_core_ByeDpiProxy_jniStartProxy(
        __attribute__((unused)) JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jint fd) {
    return internal_start_proxy(fd);
}

JNIEXPORT jint JNICALL
Java_com_samyak_netveil_core_ByeDpiProxy_jniStopProxy(
        __attribute__((unused)) JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jint fd) {
    return internal_stop_proxy(fd);
}

// -------------------------------------------------------------
// JNI Exports for io.github.dovecoteescapee.byedpi.core.ByeDpiProxy (Backward Compatibility)
// -------------------------------------------------------------

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniCreateSocketWithCommandLine(
        JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jobjectArray args) {
    return internal_create_socket_cmd(env, args);
}

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniCreateSocket(
        JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jstring ip,
        jint port,
        jint max_connections,
        jint buffer_size,
        jint default_ttl,
        jboolean custom_ttl,
        jboolean no_domain,
        jboolean desync_http,
        jboolean desync_https,
        jboolean desync_udp,
        jint desync_method,
        jint split_position,
        jboolean split_at_host,
        jint fake_ttl,
        jstring fake_sni,
        jbyte custom_oob_char,
        jboolean host_mixed_case,
        jboolean domain_mixed_case,
        jboolean host_remove_spaces,
        jboolean tls_record_split,
        jint tls_record_split_position,
        jboolean tls_record_split_at_sni,
        jint hosts_mode,
        jstring hosts,
        jboolean tfo,
        jint udp_fake_count,
        jboolean drop_sack,
        jint fake_offset) {
    return internal_create_socket(
        env, ip, port, max_connections, buffer_size, default_ttl, custom_ttl,
        no_domain, desync_http, desync_https, desync_udp, desync_method,
        split_position, split_at_host, fake_ttl, fake_sni, custom_oob_char,
        host_mixed_case, domain_mixed_case, host_remove_spaces, tls_record_split,
        tls_record_split_position, tls_record_split_at_sni, hosts_mode, hosts,
        tfo, udp_fake_count, drop_sack, fake_offset
    );
}

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniStartProxy(
        __attribute__((unused)) JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jint fd) {
    return internal_start_proxy(fd);
}

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniStopProxy(
        __attribute__((unused)) JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jint fd) {
    return internal_stop_proxy(fd);
}

