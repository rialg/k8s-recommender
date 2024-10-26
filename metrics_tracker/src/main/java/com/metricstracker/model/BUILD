load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "model",
    srcs = [
        "NodeMetric.java",
        "PodMetric.java",
    ],
    deps = [
        "//proto:metrics_java_proto",
    ],
    visibility = ["//visibility:public"],
)
