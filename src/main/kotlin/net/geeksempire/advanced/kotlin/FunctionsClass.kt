package net.geeksempire.advanced.kotlin

open class FunctionsClass (var globalInput: Int = 0) {

    interface FunctionsClass {
        fun anActionOnInput()
    }

    init {

    }

    fun anActionOnInput(input: Int) {
        println("Action On Input ::: ${globalInput + input}")
    }
}