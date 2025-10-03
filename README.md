# nonulls
Trying to avoid nulls as much as possible in java projects

## Releases
This project includes a GitHub Actions workflow that builds the project and creates a GitHub Release with JARs when you push a tag like `v0.1.0`.

How to cut a release:
- Ensure the project builds locally: `mvn clean verify`.
- Update versions as needed (parent `pom.xml` sets the `${revision}` used by modules).
- Create and push a tag following `<major>.<minor>.<patch>`.
  Example:
  - `git tag 0.1.0`
  - `git push origin 0.1.0`

The CI will:
- Build with Java 21 and run tests.
- Upload the built JARs as workflow artifacts.
- Create a GitHub Release and attach JARs for the `validator` and `jackson` modules.
- Upload snapshots to the sonatype snapshot repository.