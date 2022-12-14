# Quarkiverse - Quarkus Azure Services
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-2-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

This repository hosts Quarkus extensions for different Azure Services. You can check the [documentation of these services](https://quarkiverse.github.io/quarkiverse-docs/quarkus-azure-services/dev/index.html).

## Azure Services

The following extensions allows you to interact with some of the Azure Services:

- [Quarkus Azure App Configuration Extension](https://quarkiverse.github.io/quarkiverse-docs/quarkus-azure-services/dev/quarkus-azure-app-configuration.html): [Azure App Configuration](https://azure.microsoft.com/products/app-configuration) is a fast, scalable parameter storage for app configuration.
- [Quarkus Azure Blob Storage Extension](https://quarkiverse.github.io/quarkiverse-docs/quarkus-azure-services/dev/quarkus-azure-storage-blob.html): [Azure Blob Storage](https://azure.microsoft.com/products/storage/blobs/) is a massively scalable and secure object storage for cloud-native workloads, archives, data lakes, high-performance computing, and machine learning.

## Example applications

Example applications can be found inside the [integration-tests](integration-tests) folder:

- [Azure App Configuration sample](integration-tests/app-configuration): REST endpoint using the Quarkus extension to get the configuration stored in Azure App Configuration.
- [Azure Blob Storage samples](integration-tests/storage-blob): REST endpoint using the Quarkus extension to upload and download files to/from Azure Blob Storage.

## Contributing

Contributions are always welcome, but better create an issue to discuss them prior to any contributions.

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center"><a href="http://www.antoniogoncalves.org"><img src="https://avatars.githubusercontent.com/u/729277?v=4?s=100" width="100px;" alt="Antonio Goncalves"/><br /><sub><b>Antonio Goncalves</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/commits?author=agoncal" title="Code">💻</a> <a href="#maintenance-agoncal" title="Maintenance">🚧</a></td>
      <td align="center"><a href="https://www.linkedin.com/in/jianguo-ma-40783518/"><img src="https://avatars.githubusercontent.com/u/10357495?v=4?s=100" width="100px;" alt="Jianguo Ma"/><br /><sub><b>Jianguo Ma</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/commits?author=majguo" title="Code">💻</a> <a href="#maintenance-majguo" title="Maintenance">🚧</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
