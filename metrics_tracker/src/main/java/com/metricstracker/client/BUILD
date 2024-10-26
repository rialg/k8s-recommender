java_library(
    name = "client",
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/java/com/metricstracker/model:model",
        "//proto:metrics_java_grpc",
        "//proto:metrics_java_proto",
        "@grpc-java//stub",
        "@grpc-java//api",
    ],
    visibility = ["//visibility:public"],
)
