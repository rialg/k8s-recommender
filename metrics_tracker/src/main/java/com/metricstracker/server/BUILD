java_library(
    name = "server",
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/java/com/metricstracker/service:service",
        "//src/main/java/com/metricstracker/model:model",
        "//proto:metrics_java_grpc",
        "//proto:metrics_java_proto",
        "@grpc-java//stub",
        "@grpc-java//api",
        "@grpc-java//netty",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:org_slf4j_slf4j_simple",
    ],
    visibility = ["//visibility:public"],
)
