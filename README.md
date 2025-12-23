# graalvmBuildTest

## GitHub Actions for Native Build Release

This project uses GitHub Actions to automatically compile native applications and release them when a new tag is pushed.

## Workflows

The following GitHub Actions workflows are configured:

1. **Build Native Application** (`.github/workflows/build-native.yml`):
   - Runs on pushes to the main branch
   - Runs on pull requests to the main branch
   - Can be manually triggered
   - Builds the native application and uploads it as an artifact
   - Tests the native application execution

2. **Release Native Application** (`.github/workflows/release-native.yml`):
   - Runs when a new tag is pushed (e.g., `v1.0.0`)
   - Creates a GitHub Release with the native executable attached
   - Uses the native executable built from the tag

3. **Build Native Cross-Platform** (`.github/workflows/build-native-cross-platform.yml`):
   - Builds native executables for Linux, Windows, and macOS
   - Can be manually triggered
   - Uploads all platform executables as artifacts

## Usage

To trigger a new release:
1. Create and push a tag (e.g., `git tag v1.0.0 && git push origin v1.0.0`)
2. The workflow will automatically build the native application and create a GitHub Release
3. The native executable will be attached to the release assets

To manually trigger a cross-platform build:
1. Go to the "Actions" tab in your GitHub repository
2. Select "Build Native Cross-Platform" workflow
3. Click "Run workflow" to trigger the build

## Native Build Process

The native build process uses GraalVM to compile the Spring Boot application to a native executable. The build scripts ([buildNative.sh](buildNative.sh)) handle the necessary environment setup and compilation steps.

## Gradle Build Configuration

The project includes a [gradle.properties](gradle.properties) file that configures parallel builds and other optimizations:

- Parallel builds enabled (`org.gradle.parallel=true`)
- Build caching enabled (`org.gradle.caching=true`)
- Maximum worker threads configured (`org.gradle.workers.max=4`)
- Daemon process enabled for faster builds

## Environment Variables Configuration

This project uses environment variables for sensitive configuration values. Create a `.env` file in the root directory or set the following environment variables:

- `RABBITMQ_PASSWORD`: Password for RabbitMQ connection
- `MYSQL_PASSWORD`: Password for MySQL database connection
- `MONGODB_PASSWORD`: Password for MongoDB connection
- `REDIS_PASSWORD`: Password for Redis connection

A sample `.env.example` file has been provided in the project root directory. Copy it to `.env` and update the values as needed:

```bash
cp .env.example .env
# Then edit .env with your actual passwords
```