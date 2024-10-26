# Makefile for LoadController Julia Project

# Variables
JULIA = julia
PROJECT_NAME = LoadController
TEST_DIR = test
SRC_DIR = src
BUILD_DIR = build
DOCS_DIR = docs

# Default target
.PHONY: all
all: build test

# Create project structure
.PHONY: init
init:
	@echo "Creating project structure..."
	@mkdir -p $(SRC_DIR)
	@mkdir -p $(TEST_DIR)
	@mkdir -p $(BUILD_DIR)
	@mkdir -p $(DOCS_DIR)
	@if [ ! -f Project.toml ]; then \
		$(JULIA) --project=. -e 'using Pkg; Pkg.generate("$(PROJECT_NAME)")'; \
	fi
	@$(JULIA) --project=. -e 'using Pkg; Pkg.add(["StatsBase", "Statistics", "LinearAlgebra", "Distributions", "Dates", "ARCHModels", "StateSpaceModels", "Test"])'

# Copy source files
.PHONY: build
build: init
	@echo "Building project..."
	@if [ ! -f $(SRC_DIR)/$(PROJECT_NAME).jl ]; then \
		echo "Error: Source file $(SRC_DIR)/$(PROJECT_NAME).jl not found"; \
		exit 1; \
	fi
	@$(JULIA) --project=. -e 'using Pkg; Pkg.resolve(); Pkg.instantiate()'
	@$(JULIA) --project=. -e 'using Pkg; Pkg.precompile()'

# Run tests
.PHONY: test
test: build
	@echo "Running tests..."
	@$(JULIA) --project=. -e 'using Pkg; Pkg.test()'

# Run specific test file
.PHONY: test-file
test-file:
	@echo "Running specific test file..."
	@$(JULIA) --project=. $(TEST_DIR)/$(file)

# Clean build artifacts
.PHONY: clean
clean:
	@echo "Cleaning build artifacts..."
	@rm -rf $(BUILD_DIR)/*
	@rm -rf Manifest.toml
	@find . -name "*.cov" -type f -delete

# Install dependencies
.PHONY: deps
deps:
	@echo "Installing dependencies..."
	@$(JULIA) --project=. -e 'using Pkg; Pkg.resolve(); Pkg.instantiate()'

# Update dependencies
.PHONY: update
update:
	@echo "Updating dependencies..."
	@$(JULIA) --project=. -e 'using Pkg; Pkg.update()'

# Run REPL with project environment
.PHONY: repl
repl:
	@$(JULIA) --project=.

# Generate documentation
.PHONY: docs
docs:
	@echo "Generating documentation..."
	@$(JULIA) --project=. -e 'using Pkg; Pkg.add("Documenter"); using Documenter; include("$(DOCS_DIR)/make.jl")'

# Help target
.PHONY: help
help:
	@echo "Available targets:"
	@echo "  all          - Build project and run tests (default)"
	@echo "  init         - Initialize project structure and dependencies"
	@echo "  build        - Build the project"
	@echo "  test         - Run all tests"
	@echo "  test-file    - Run specific test file (use make test-file file=filename.jl)"
	@echo "  clean        - Clean build artifacts"
	@echo "  deps         - Install dependencies"
	@echo "  update       - Update dependencies"
	@echo "  repl         - Start Julia REPL with project environment"
	@echo "  docs         - Generate documentation"
	@echo "  help         - Show this help message"

# Project structure check
.PHONY: check-structure
check-structure:
	@echo "Checking project structure..."
	@test -d $(SRC_DIR) || (echo "$(SRC_DIR) directory missing" && exit 1)
	@test -d $(TEST_DIR) || (echo "$(TEST_DIR) directory missing" && exit 1)
	@test -d $(BUILD_DIR) || (echo "$(BUILD_DIR) directory missing" && exit 1)
	@test -d $(DOCS_DIR) || (echo "$(DOCS_DIR) directory missing" && exit 1)
	@test -f Project.toml || (echo "Project.toml missing" && exit 1)
