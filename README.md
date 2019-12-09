# JavaWrapperGnuPG
tested with gpg (GnuPG) 2.2.17 - libgcrypt 1.8.4



## Usage:

````
// set gpg binary, homeDir is optinal, can be null
GnuPG2 gpg = GnuPG2(String binGPG, String homeDir)

// does not work at all, autodetection is missing
/* !! GnuPG2 gpg = GnuPG2(String homeDir) !! */

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

### Autodetection for gpg is missing

Constructor

````
GnuPG2(String homeDir)
````

does not work at all. Autodetection should be implemented.



### On Windows sdtout of GnuPG may have encoding errors

Windows is not using UTF-8.