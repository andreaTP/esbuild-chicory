[![CI](https://github.com/andreaTP/esbuild-chicory/workflows/CI/badge.svg)](https://github.com/andreaTP/esbuild-chicory)

# POC Portable esbuild for Java

This is a thin layer to allow porting esbuild to architectures/OSes that are not supported by the upstream project but that can run Java.
We achieve the goal by running esbuild on top of Chicory.

> **NOTE:** `esbuild` only runs in pure interpreted mode, and it's really slow, but is a good use case to track the progress on the AOT compiler

## Build

Prepare the environment and the test project:

```bash
./prepare.sh
```

Compile and run the integration test:

```bash
mvn -B clean install
```
