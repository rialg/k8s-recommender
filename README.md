# k8s-recommender

> **⚠️ Disclaimer:** This project is currently experimental and part of an ongoing Master's thesis at UNIR ([Universidad Internacional de La Rioja](https://unir.net)). It serves as a brain dump of ideas and implementations, and should not be considered production-ready.

## Overview

k8s-recommender is a Kubernetes-native system that provides intelligent resource allocation and scaling recommendations for containerized applications. It combines metrics collection, load analysis, and optimization to suggest optimal resource configurations.

## Project Structure

The project consists of four main components:

- **metrics_tracker**: Java-based service for collecting and processing Kubernetes metrics using Prometheus
- **solver**: Julia-based optimization engine for computing resource recommendations
- **scaling_controller**: Kubernetes controller for managing application provisioning and scaling
- **load_controller**: Julia-based component for load analysis and prediction

## Prerequisites

- Java 21 or higher
- Julia 1.0 or higher
- Kubernetes cluster (1.18+)
- Prometheus installed in the cluster
- Bazel and Make build systems

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/k8s-recommender.git
   cd k8s-recommender
   ```

2. Build the components:
   ```bash
   # Build metrics tracker
   cd metrics_tracker
   bazel build //:MetricsTrackerServer

   # Build scaling controller
   cd ../scaling_controller
   bazel build //:AppProvisionerReconciller

   # Build Julia components
   cd ../solver
   make init && make test

   cd ../load_controller
   make init && make test
   ```

## Architecture

```plaintext
┌──────────────────┐    ┌──────────────────┐
│  Metrics Tracker │───>│      Solver      │
└──────────────────┘    └──────────────────┘
         │                       │
         │                       │
         ▼                       ▼
┌──────────────────┐    ┌──────────────────┐
│ Load Controller  │<───│Scaling Controller│
└──────────────────┘    └──────────────────┘
```

## Pending Enhancements

- **Distribution using Docker containers and Helm**: Package the components as Docker containers and create Helm charts for deployment in Kubernetes environments.
- **Versioning**: Establish a versioning scheme to track and manage project releases and compatibility.
- **Testing**: Develop unit tests, integration tests, and continuous testing pipelines to ensure robustness and reliability.
- **Module Integration**: Improve the integration between modules to streamline data flow and enhance performance.

## Contributing

As this is an experimental project as part of academic research, please open an issue first to discuss any changes you would like to make.

## License

This project is licensed under the terms of the included LICENSE.md file.

## Academic Context

This project is being developed as part of a Master's thesis at UNIR (Universidad Internacional de La Rioja) in Mathematical Engineering and Computer Science. The goal is to explore and implement novel approaches to resource optimization in containerized environments.

## Status

🚧 **Under Active Development** 

This project is in active development, and many features are experimental or incomplete. The API and implementation details are subject to change.

## Contact

For academic inquiries or collaboration opportunities, please open an issue in this repository.

---

**Note:** This project is not intended for production use at this stage. It serves as a research platform and proof of concept for academic purposes.
