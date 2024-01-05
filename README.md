<!-- Thanks to https://github.com/othneildrew/Best-README-Template/ -->
<a name="readme-top"></a>

<!-- PROJECT SHIELDS -->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/stoerr/jcr-actions-for-GPTs">
    <img src="images/logo.png" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">JCR Repository access actions for GPTs</h3>

  <p align="center">
    This provides the action (incl. an OpenAPI declaration) useable for GPTs to read the JCR content repository
    in Apache Sling / AEM on configured paths.
    <br />
    <a href="https://github.com/stoerr/jcr-actions-for-GPTs"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/stoerr/jcr-actions-for-GPTs">View Demo</a>
    ·
    <a href="https://github.com/stoerr/jcr-actions-for-GPTs/issues">Report Bug</a>
    ·
    <a href="https://github.com/stoerr/jcr-actions-for-GPTs/issues">Request Feature</a>
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->

## About The Project

[![Product Name Screen Shot][product-screenshot]](https://example.com)

This is an OSGI bundle deployable on [Apache Sling](https://sling.apache.org/) /
[Adobe Experience Manager (AEM)](https://business.adobe.com/uk/products/experience-manager/adobe-experience-manager.html)
that provides the actions for a
GPT to read the JCR content repository on configured paths. You can use it to direct ChatGPT to read the properties of
(anonymously readable) JCR nodes and to read the contents of stored files, and answer questions, do summary tasks etc.

This is currently just done as minimal project so that it does what I need it to do. If it doesn't work for you
please contact me - if there is somebody who is interested and willing to provide feedback and share his experiences,
I'll be happy to help and extend it!

Preconditions: To use it you have to be a paying OpenAI ChatGPT customer,
since otherwise GPTs aren't available for you.

Configuration: There is an OSGI configuration "ChatGPT JCR Actions" that
configures the paths to be read, and an required API key for authentication.
The API key should be a long random key
that you can paste both into the configuration and into the authentication section of the GPT, and is responsible for
the security. Additionally there is a number of regular expressions at which read access is permitted. The full path has
to match any of the keys.

To be reachable from ChatGPT it needs to be accessible from the internet via HTTPS. The easiest way I know is using
[serveo.net](https://serveo.net/). You can start it with:

```
ssh -T -R yourdomain.serveo.net:80:localhost:8080 serveo.net
```

Replace `yourdomain` with a prefix of your choice that contains some randomness to avoid collisions and replace 8080
with the port you are using for Apache Sling. The servlet
only works if it was configured and is available at the URL /bin/public/gpt/jcractions .

Configure it as actions from a GPT: generally that works like
[GPT creation for Co-Developer GPT Engine](https://codevelopergptengine.stoerr.net/gpt.html) ,
but you have to use the import URL `https://yourdomain.serveo.net/bin/gpt/jcractions.yaml`.
You can either create a GPT that just has this as actions, or
add this as additional actions to a Co-Developer GPT.
IMPORTANT: you have to set the authentication type to "API Key", and paste the same API key that you configured in the
OSGI configuration into the API key field and set Auth Type to Custom with custom header name `X-JcrActions-Api-Key`.
You have to repeat that if you re-import the actions, or ChatGPT gives an empty answer with ClientError (which is a bug
in ChatGPT).

CAUTION: if you do it like that it makes your local server reachable from the internet at `yourdomain.serveo.net`! If
you have proprietary stuff on your server and / or use admin/admin as password, and that worries you, please configure a
reverse proxy or something like that so that only that URL /bin/public/gpt/jcractions is reachable from the internet.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- 
### Built With

* [![Maven][Maven-shield]][Maven-url]
* [![Apache Sling][Sling-shield]][Sling-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>
 -->


<!-- GETTING STARTED -->

## Getting Started

### Prerequisites

### Installation

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- USAGE EXAMPLES -->

## Usage

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- ROADMAP -->

## Roadmap

<!-- 
- [ ] Feature 1
- [ ] Feature 2
- [ ] Feature 3
    - [ ] Nested Feature
-->

See the [open issues](https://github.com/stoerr/jcr-actions-for-GPTs/issues) for a full list of proposed features (and
known issues).

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- CONTRIBUTING -->

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any
contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also
simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- LICENSE -->

## License

Distributed under the MIT License. See `LICENSE.txt` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- CONTACT -->

## Contact

Hans-Peter Störr - [www.stoerr.net](https://www.stoerr.net) , [@HansPeterStoerr](https://twitter.com/HansPeterStoerr)

Project Link: [https://github.com/stoerr/jcr-actions-for-GPTs](https://github.com/stoerr/jcr-actions-for-GPTs)

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- ACKNOWLEDGMENTS -->

<!-- ## Acknowledgments

* []()
* []()
* []()

<p align="right">(<a href="#readme-top">back to top</a>)</p>
-->


<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->

[contributors-shield]: https://img.shields.io/github/contributors/stoerr/jcr-actions-for-GPTs.svg?style=for-the-badge

[contributors-url]: https://github.com/stoerr/jcr-actions-for-GPTs/graphs/contributors

[forks-shield]: https://img.shields.io/github/forks/stoerr/jcr-actions-for-GPTs.svg?style=for-the-badge

[forks-url]: https://github.com/stoerr/jcr-actions-for-GPTs/network/members

[stars-shield]: https://img.shields.io/github/stars/stoerr/jcr-actions-for-GPTs.svg?style=for-the-badge

[stars-url]: https://github.com/stoerr/jcr-actions-for-GPTs/stargazers

[issues-shield]: https://img.shields.io/github/issues/stoerr/jcr-actions-for-GPTs.svg?style=for-the-badge

[issues-url]: https://github.com/stoerr/jcr-actions-for-GPTs/issues

[license-shield]: https://img.shields.io/github/license/stoerr/jcr-actions-for-GPTs.svg?style=for-the-badge

[license-url]: https://github.com/stoerr/jcr-actions-for-GPTs/blob/master/LICENSE.txt

[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555

[linkedin-url]: https://linkedin.com/in/hans-peter-störr-5944594

[product-screenshot]: images/screenshot.png
