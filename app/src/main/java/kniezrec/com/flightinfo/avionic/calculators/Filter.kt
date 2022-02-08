package kniezrec.com.flightinfo.avionic.calculators

import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */
object Filter {

  private const val ALPHA = 0.25F

  fun lowPass(input: FloatArray, output: FloatArray): FloatArray {
    if (input.size != output.size) {
      Timber.d("Different array sizes. ${input.size} != ${output.size}")
      return input
    }

    input.forEachIndexed { i, item ->
      output[i] = output[i] + ALPHA * (item - output[i])
    }

    return output
  }

  fun lowPass(input: Float, output: Float): Float {
    return output + ALPHA * (input - output)
  }

  fun lowPass(input: Double, output: Double): Double {
    return output + ALPHA * (input - output)
  }
}