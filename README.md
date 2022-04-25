# JVP
one JS obfuscator for onetap V4 (deprecated)
```code
Due to the deprecated of relevant usage scenarios, some functionality is now making public 
, and it's no longer maintained (but still work).
```

### Description
- Designed for Onetap(https://www.onetap.com/) JS
- Work under the assumption that Onetap JS Evluator makes sure function ``` Cheat.GetUsername ``` never got rewritten
- It build one unique file for each onetap user
- It convert every Property Call to Element Call, and add a mapper from each calls to the actual Onetap function, strings of element call was encoded so that it can only be decoded with the correct username.
- This program was written in 2020, game rules might already changed.

### Advantages and Disadvantage
#### Advantages
- It's hardly possible for someone to load the javascript that don't belong to him(that means, that file wasn't encrypt with his username)
- It's hardly possible for people who don't know what the username was used to encrypt the JS to decrypt it.
- The efficiency during program runs does not get significant affect.
#### Disadvantages
- It's hard but possible for people who know what the username was used to encrypt the JS to decrypt it.
- Loading the Javascript take times, because of those string decrypt and bad crypto

### String Encrypt Methods
- <b>Kim64</b>: A changed version of Base64, with random alphabat order and xor in raw bytes 
- <b>Magnify_String</b>: changed all raw string to the same length, so people wont be able to guess the raw string by the length of Kim64 string
- <b>DecodeString</b>: Decode bytes with Xor keys to string, the Xor keys are generated from username, same username always provide same key.

### UI


