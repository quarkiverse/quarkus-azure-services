# Quarkiverse - Quarkus Azure Services

[![Build](https://github.com/quarkiverse/quarkus-azure-services/workflows/Build/badge.svg?branch=main)](https://github.com/quarkiverse/quarkus-azure-services/actions?query=workflow%3ABuild)
[![License](https://img.shields.io/github/license/quarkiverse/quarkus-azure-services.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Central](https://img.shields.io/maven-central/v/io.quarkiverse.azureservices/quarkus-azure-services-parent?color=green)](https://central.sonatype.com/artifact/io.quarkiverse.azureservices/quarkus-azure-services-parent)
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-17-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

This repository hosts Quarkus extensions for a selection of popular Azure Services. The official documentation of these services is in Quarkiverse at [Quarkus Azure Services](https://docs.quarkiverse.io/quarkus-azure-services/dev/index.html). Each extension is functionally independent of the others, but they can be used together.

## Azure Services

Here's the current selection of Quarkus Azure Services:

- [Quarkus Azure App Configuration Extension](https://docs.quarkiverse.io/quarkus-azure-services/dev/quarkus-azure-app-configuration.html): [Azure App Configuration](https://azure.microsoft.com/products/app-configuration)
  is a fast, scalable parameter storage for app configuration.
- [Quarkus Azure Cosmos DB Extension](https://docs.quarkiverse.io/quarkus-azure-services/dev/quarkus-azure-cosmos.html): [Azure Cosmos DB](https://azure.microsoft.com/products/cosmos-db) is a fully managed NoSQL, relational, and vector database.
- [Quarkus Azure Key Vault Extension](https://docs.quarkiverse.io/quarkus-azure-services/dev/quarkus-azure-key-vault.html): [Azure Key Vault](https://azure.microsoft.com/products/key-vault) is a cloud service for securely storing and accessing secrets.
- [Quarkus Azure Blob Storage Extension](https://docs.quarkiverse.io/quarkus-azure-services/dev/quarkus-azure-storage-blob.html): [Azure Blob Storage](https://azure.microsoft.com/products/storage/blobs/)
  is a massively scalable and secure object storage for cloud-native workloads, archives, data lakes, high-performance
  computing, and machine learning.
- [Quarkus Azure Event Hubs Extension](https://docs.quarkiverse.io/quarkus-azure-services/dev/quarkus-azure-eventhubs.html): [Azure Event Hubs](https://azure.microsoft.com/products/event-hubs)
  is a big data streaming platform and event ingestion service. It can receive and process millions of events per second.

## Azure Services outside this repository
- [Quarkus Opentelemetry Exporter for Microsoft Azure](https://docs.quarkiverse.io/quarkus-opentelemetry-exporter/dev/quarkus-opentelemetry-exporter-azure.html) enables [Azure Application Insights](https://learn.microsoft.com/en-us/azure/azure-monitor/app/app-insights-overview) telemetry for Quarkus native applications

## Example applications

Example applications can be found inside the [integration-tests](integration-tests) folder:

- [Azure App Configuration sample](integration-tests/azure-app-configuration): REST endpoint using the Quarkus extension
  to get the configuration stored in Azure App Configuration.
- [Azure Cosmos DB sample](integration-tests/azure-cosmos): REST endpoint using the Quarkus extension to implement CRUD operations in Azure Cosmos DB.
- [Azure Key Vault sample](integration-tests/azure-keyvault): REST endpoint using the Quarkus extension
  to:
    - Create a secret via [SecretClient](https://learn.microsoft.com/java/api/com.azure.security.keyvault.secrets.secretclient) and [SecretAsyncClient](https://learn.microsoft.com/java/api/com.azure.security.keyvault.secrets.secretasyncclient) in Azure Key Vault.
    - Load a secret from Azure Key Vault as property using `ConfigProperty`.
- [Azure Blob Storage sample](integration-tests/azure-storage-blob): REST endpoint using the Quarkus extension to
  upload and download files to/from Azure Blob Storage.
- [Azure Event Hubs sample](integration-tests/azure-eventhubs): REST endpoint using the Quarkus extension to send/receive data to/from Azure Event Hubs.

## How to release a new version

Follow [this wiki](https://github.com/quarkiverse/quarkiverse/wiki/Release) to release a new version of the extensions.
You can reference the following PRs as examples:

* Release a new version: https://github.com/quarkiverse/quarkus-azure-services/pull/316.
* Register new extensions in catalog: https://github.com/quarkusio/quarkus-extension-catalog/pull/64.
  See [Publish your extension in registry.quarkus.io](https://quarkus.io/guides/writing-extensions#publish-your-extension-in-registry-quarkus-io)
  for more information.

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
      <td align="center" valign="top" width="14.28%"><a href="https://www.linkedin.com/in/jianguo-ma-40783518/"><img src="https://avatars.githubusercontent.com/u/10357495?v=4?s=100" width="100px;" alt="Jianguo Ma"/><br /><sub><b>Jianguo Ma</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/commits?author=majguo" title="Code">💻</a> <a href="#maintenance-majguo" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="http://www.antoniogoncalves.org"><img src="https://avatars.githubusercontent.com/u/729277?v=4?s=100" width="100px;" alt="Antonio Goncalves"/><br /><sub><b>Antonio Goncalves</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/commits?author=agoncal" title="Code">💻</a> <a href="#maintenance-agoncal" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="http://www.radcortez.com"><img src="https://avatars.githubusercontent.com/u/5796305?v=4?s=100" width="100px;" alt="Roberto Cortez"/><br /><sub><b>Roberto Cortez</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/commits?author=radcortez" title="Code">💻</a> <a href="https://github.com/quarkiverse/quarkus-azure-services/pulls?q=is%3Apr+reviewed-by%3Aradcortez" title="Reviewed Pull Requests">👀</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://twitter.com/ppalaga"><img src="https://avatars.githubusercontent.com/u/1826249?v=4?s=100" width="100px;" alt="Peter Palaga"/><br /><sub><b>Peter Palaga</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/commits?author=ppalaga" title="Code">💻</a> <a href="https://github.com/quarkiverse/quarkus-azure-services/pulls?q=is%3Apr+reviewed-by%3Appalaga" title="Reviewed Pull Requests">👀</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://lesincroyableslivres.fr/"><img src="https://avatars.githubusercontent.com/u/1279749?v=4?s=100" width="100px;" alt="Guillaume Smet"/><br /><sub><b>Guillaume Smet</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/commits?author=gsmet" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="http://gastaldi.wordpress.com"><img src="https://avatars.githubusercontent.com/u/54133?v=4?s=100" width="100px;" alt="George Gastaldi"/><br /><sub><b>George Gastaldi</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/commits?author=gastaldi" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/JoaoBrandao"><img src="https://avatars.githubusercontent.com/u/13374459?v=4?s=100" width="100px;" alt="João Brandão"/><br /><sub><b>João Brandão</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/issues?q=author%3AJoaoBrandao" title="Bug reports">🐛</a></td>
    </tr>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="http://melloware.com"><img src="https://avatars.githubusercontent.com/u/4399574?v=4?s=100" width="100px;" alt="Melloware"/><br /><sub><b>Melloware</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/issues?q=author%3Amelloware" title="Bug reports">🐛</a> <a href="https://github.com/quarkiverse/quarkus-azure-services/pulls?q=is%3Apr+reviewed-by%3Amelloware" title="Reviewed Pull Requests">👀</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://ridingthecrest.com/"><img src="https://avatars.githubusercontent.com/u/75821?v=4?s=100" width="100px;" alt="Ed Burns"/><br /><sub><b>Ed Burns</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/pulls?q=is%3Apr+reviewed-by%3Aedburns" title="Reviewed Pull Requests">👀</a> <a href="https://github.com/quarkiverse/quarkus-azure-services/commits?author=edburns" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/backwind1233"><img src="https://avatars.githubusercontent.com/u/4465723?v=4?s=100" width="100px;" alt="zhihaoguo"/><br /><sub><b>zhihaoguo</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/pulls?q=is%3Apr+reviewed-by%3Abackwind1233" title="Reviewed Pull Requests">👀</a> <a href="https://github.com/quarkiverse/quarkus-azure-services/commits?author=backwind1233" title="Code">💻</a> <a href="#maintenance-backwind1233" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://thejavaguy.org/"><img src="https://avatars.githubusercontent.com/u/11942401?v=4?s=100" width="100px;" alt="Ivan Milosavljević"/><br /><sub><b>Ivan Milosavljević</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/commits?author=TheJavaGuy" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="http://oscerd.github.io"><img src="https://avatars.githubusercontent.com/u/5106647?v=4?s=100" width="100px;" alt="Andrea Cosentino"/><br /><sub><b>Andrea Cosentino</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/commits?author=oscerd" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://automatiko.io"><img src="https://avatars.githubusercontent.com/u/904474?v=4?s=100" width="100px;" alt="Maciej Swiderski"/><br /><sub><b>Maciej Swiderski</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/commits?author=mswiderski" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/fhavel"><img src="https://avatars.githubusercontent.com/u/42615282?v=4?s=100" width="100px;" alt="Frantisek Havel"/><br /><sub><b>Frantisek Havel</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/commits?author=fhavel" title="Code">💻</a></td>
    </tr>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/galiacheng"><img src="https://avatars.githubusercontent.com/u/59823457?v=4?s=100" width="100px;" alt="Galia Cheng"/><br /><sub><b>Galia Cheng</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/commits?author=galiacheng" title="Code">💻</a> <a href="#maintenance-galiacheng" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/jeanbisutti"><img src="https://avatars.githubusercontent.com/u/14811066?v=4?s=100" width="100px;" alt="Jean Bisutti"/><br /><sub><b>Jean Bisutti</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/commits?author=jeanbisutti" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://hollycummins.com"><img src="https://avatars.githubusercontent.com/u/11509290?v=4?s=100" width="100px;" alt="Holly Cummins"/><br /><sub><b>Holly Cummins</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-azure-services/issues?q=author%3Aholly-cummins" title="Bug reports">🐛</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification.
Contributions of any kind welcome!
