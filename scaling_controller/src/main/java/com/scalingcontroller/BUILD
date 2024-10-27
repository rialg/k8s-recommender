load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "operator",
    srcs = glob(["*.java"]),
    deps = [
        "@maven//:io_javaoperatorsdk_operator_framework",
        "@maven//:io_fabric8_kubernetes_client_api",
        "@maven//:io_fabric8_kubernetes_model_apps",
        "@maven//:io_fabric8_kubernetes_model_common",
        "@maven//:io_fabric8_kubernetes_model_core",
        "@maven//:io_javaoperatorsdk_operator_framework_core",
        "@maven//:io_fabric8_kubernetes_client",
        "@maven//:io_fabric8_kubernetes_model",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:ch_qos_logback_logback_classic",
    ],
    visibility = ["//visibility:public"],
)
