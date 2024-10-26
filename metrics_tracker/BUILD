load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
    name = "MetricsTrackerServer",
    main_class = "com.metricstracker.server.MetricsTrackerServer",
    runtime_deps = [
        "@grpc-java//api",
        "@grpc-java//netty",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:org_slf4j_slf4j_simple",
        "//src/main/java/com/metricstracker/server:server"
    ]
)


java_binary(
    name = "MetricsTrackerClient",
    main_class = "com.metricstracker.client.MetricsTrackerClient",
    runtime_deps = [
        "//src/main/java/com/metricstracker/client:client",
        "@grpc-java//api",
        "@grpc-java//netty",
    ],
    visibility = ["//visibility:public"],
)
