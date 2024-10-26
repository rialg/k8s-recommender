java_test(
    name = "server_test",
    srcs = glob(["server/*Test.java"]),
    deps = [
        "//src/main/java/com/metricstracker:server",
        "//src/main/java/com/metricstracker:service",
        "@maven//:junit_junit",
        "@maven//:org_mockito_mockito_core",
        "@io_grpc_grpc_java//testing",
    ],
)
