package ndd

import com.google.common.hash.Hashing

object NearDuplicateDetection {
    private fun List<String>.shingle(size: Int) = windowed(size)

    /**
     * Converts a bytearray to hex.
     */
    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }

    /**
     * Simply generates primes for a sequence.
     * Use as: primes().take(<numbertotake>)
     */
    private fun primes(): Sequence<Int> {
        var i = 0
        return sequence {
            generateSequence { i++ }
                .filter { n -> n > 1 && ((2 until n).none { i -> n % i == 0 }) }
                .forEach { yield(it) }
        }
    }

    private val seeds = primes().take(84).toList()

    /**
     * Generates hashes based on the murmur3_128 function,
     */
    private val murmurHashes = seeds.map {
        { shingle: List<String> ->
            Hashing.murmur3_128(it).hashBytes(shingle.joinToString().toByteArray()).asBytes().toHex()
        }
    }

    fun minHashes(text: String, functions: List<(List<String>) -> String> = murmurHashes): List<String> {
        val shingles = text.split("\\W".toRegex()).shingle(4)
        return functions.map { hashFunction -> shingles.map { hashFunction(it) }.min() ?: "" }
    }

    /**
     * Calculates the jaccardpercentage for two shingles. The shingles are hashed strings.
     * Generates a summed list where two equal shingles adds a 1 to the sum, two unequal adds nothing.
     * Then divides by the size of shingle 1 to get a percentage
     */
    private fun jaccardPercent(shingle1: List<String>, shingle2: List<String>) =
        shingle1.zip(shingle2).sumBy { if (it.first == it.second) 1 else 0 } / shingle1.size.toDouble()

    /**
     * Simply calls jaccard percentage, and checks if the two shingles are equal equal to one another
     * by a percentage greater than 0.9.
     */
    fun isNearDuplicate(hashedShingles1: List<String>, hashedShingles2: List<String>): Boolean {
        return jaccardPercent(hashedShingles1, hashedShingles2) >= 0.9
    }
}