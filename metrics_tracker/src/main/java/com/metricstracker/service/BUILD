java_library(
    name = "service",
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/java/com/metricstracker/model:model",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:org_slf4j_slf4j_simple",
        "@maven//:io_kubernetes_client_java",
        "@maven//:io_kubernetes_client_java_api",
    ],
    visibility = ["//visibility:public"],
)
