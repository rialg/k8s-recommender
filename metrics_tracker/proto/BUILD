load("@rules_proto//proto:defs.bzl", "proto_library")
load("@rules_java//java:defs.bzl", "java_proto_library")
load("@grpc-java//:java_grpc_library.bzl", "java_grpc_library")

# Define the proto_library for the .proto files
proto_library(
    name = "metrics_proto",
    srcs = ["metrics.proto"],
    visibility = ["//visibility:public"],
)

# Generate Java classes from the proto file
java_proto_library(
    name = "metrics_java_proto",
    deps = [":metrics_proto"],
    visibility = ["//visibility:public"],
)

# Generate gRPC Java classes from the proto file
java_grpc_library(
    name = "metrics_java_grpc",
    srcs = [":metrics_proto"],
    deps = [":metrics_java_proto"],
    visibility = ["//visibility:public"],
)
