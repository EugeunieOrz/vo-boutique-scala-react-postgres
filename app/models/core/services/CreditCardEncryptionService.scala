package models.core.services

/**
 * Author: Ievgeniia Ozirna
 *
 * Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */

import javax.inject.{ Inject, Singleton }

import play.api.Configuration

import java.util.UUID

import java.nio.charset.StandardCharsets
import com.goterl.lazycode.lazysodium.LazySodium
import com.goterl.lazycode.lazysodium.LazySodiumJava
import com.goterl.lazycode.lazysodium.SodiumJava
import com.goterl.lazycode.lazysodium.exceptions.SodiumException
import com.goterl.lazycode.lazysodium.interfaces._
import com.goterl.lazycode.lazysodium.utils.Key
import com.goterl.lazycode.lazysodium.utils.KeyPair
import com.sun.jna.NativeLong

/**
 * For every service you need, you should specify a specific crypto service with its own keys.
 *
 * That is, if you have a service which encrypts credit cards, and another service which encrypts S3 credentials, they
 * should not reuse the key.  If you use the same key for both, then an attacker can cross reference between
 * the encrypted values and reconstruct the key.  This rule applies even if you are sharing the same key for hashing
 * and encryption.
 *
 * Keeping distinct keys per service is known as the "key separation principle".
 */
@Singleton
class CreditCardEncryptionService @Inject() (configuration: Configuration) {

  val sodium = new SodiumJava()
  val lazySodium = new LazySodiumJava(sodium, StandardCharsets.UTF_8)
  val key = lazySodium.cryptoSecretBoxKeygen()

  @throws(classOf[SodiumException])
  def encrypt(msg: String) = {
    // Symmetric key encryption
    val nonce = lazySodium.nonce(SecretBox.NONCEBYTES)
    val encrypted = lazySodium.cryptoSecretBoxEasy(msg, nonce, key)
    (encrypted, nonce, key)
  }

  @throws(classOf[SodiumException])
  def decrypt(msg: String, nonce: Array[Byte]) = {
    // Symmetric key encryption
    val keyBytes = key.getAsBytes()
    val cipherBytes = LazySodium.toBin(msg)
    val messageBytes = new Array[Byte](cipherBytes.length - SecretBox.MACBYTES)

    if (!lazySodium.cryptoSecretBoxOpenEasy(messageBytes, cipherBytes, cipherBytes.length, nonce, keyBytes)) {
      "Could not decrypt message."
    } else {
      lazySodium.str(messageBytes)
    }
  }

}
