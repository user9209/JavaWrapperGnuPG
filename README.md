# JavaWrapperGnuPG
tested with GnuPG 2.2.17 - libgcrypt 1.8.4



## Usage:

````
// set gpg binary, homeDir is optinal, can be null
GnuPG2 gpg = GnuPG2(String binGPG, String homeDir)
GnuPG2 gpg = GnuPG2(String homeDir)

// list keys
gpg.listKeys()

// import key
gpg.importKeyFile(keyFileSecret)

// get fingerprint
gpg.fingerprintKeyFile(keyFileSecret)
Files.readString(keyData)

// encrypt
String ciphertext = gpg.encrypt(data, identityFingerprintOrMail);

// decrypt
String plaintext = gpg.decrypt(ciphertext);
````







## Well known tasks

None



## Related work

[Yaniv Yemini @2004](http://www.macnews.co.il/mageworks/java/gnupg/)

[John Anderson @2002](https://lists.gnupg.org/pipermail/gnupg-devel/2002-February/018098.html)
