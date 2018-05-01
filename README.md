
| [Java](https://github.com/davidcarboni/cryptolite-java "Java implementation")                                                      | [Python](https://github.com/davidcarboni/cryptolite-python "Python implementation")                                                  | [Go](https://github.com/davidcarboni/cryptolite-go "Go implementation")                                                      |
| ------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| [![Build Status](https://travis-ci.org/davidcarboni/cryptolite-java.svg)](https://travis-ci.org/davidcarboni/cryptolite-java) | [![Build Status](https://travis-ci.org/davidcarboni/cryptolite-python.svg)](https://travis-ci.org/davidcarboni/cryptolite-python) | [![Build Status](https://travis-ci.org/davidcarboni/cryptolite-go.svg)](https://travis-ci.org/davidcarboni/cryptolite-go) |


Cryptolite
----------


### What is it?

Cryptolite is a wrapper for the Java Cryptography Extension, providing simple, "right" cryptography and bypassing all the options. It's "lite" as in "easy to use", not as in "less powerful". It's similar to Jasypt, (http://www.jasypt.org/) but provides even less options - if any. This means Cryptolite can do serious cryptography in just half a dozen classes and a tiny number of API methods. 

Cryptolite doesn't do any cryptography itself. Instead it relies on the well known open source BouncyCastle JCE provider to do the heavy lifting. The API is focused explicitly on providing the things developers need, especially webapp developers - hashing passwords, generating random IDs, encrypting Strings and Files, digital signatures and key exchange. No options means under the covers it just does what's most appropriate - and if necessary pragmatic - enabling you to use cryptography without having to understand it in depth. For example, did you know that using AES in ECB mode is a bad idea? Neither did I when I started, so I wrote Cryptolite to take care of it. (http://www.codinghorror.com/blog/2009/05/why-isnt-my-encryption-encrypting.html)


### See it in action

If you'd like to see a showcase app I put together with Cryptolite, have a look at https://cryptonite.herokuapp.com (it may take a while to load - it's a Heroku thing). Cryptonite also provides Encryption-as-a-service: https://cryptonite.herokuapp.com/api


### Maven usage

To use Cryptolite in your project:

		<dependency>
			<groupId>com.github.davidcarboni</groupId>
			<artifactId>cryptolite</artifactId>
			<version>1.3.2</version>
		</dependency>


### Why release it?

To share what I believe to be a valuable by-product of my last startup, Workdocx with the community, (inspired by 37signals: http://37signals.com/svn/posts/1620-sell-your-by-products).

The Cryptolite library was developed to provide security for the Workdocx service and is the result of many hours of research and coding work I did to keep our users safe online.

For the community, the hope is that this will make it such a no-brainer to use good cryptography that more and more people will get it right by default. Keeping it small and opening the source code makes it easy to see how to make use of the JCE in ways not provided for by Cryptolite, so let me know via pull request if you'd like to feed something back in.


### Thanks go to..

Much of the inspiration for the algorithms, modes and parameters comes from http://www.daemonology.net/blog/2009-06-11-cryptographic-right-answers.html. Additional research is from good old Wikipedia, as well as a bunch of other sites offering advice, including Coding Horror, above. JCE bootstrapping is from Beginning Cryptography With Java (http://www.wrox.com/WileyCDA/WroxTitle/Beginning-Cryptography-with-Java.productCd-0764596330.html), and many thanks go to the guys at BouncyCastle for their JCE provider (http://www.bouncycastle.org/).


### Licensing:

This library is released under the MIT license, like many other libraries, which means you are free to use it in commercial products.

If you have any questions, feel free to contact me via @davidcarboni or find me on GitHub at https://github.com/davidcarboni.

David Carboni

