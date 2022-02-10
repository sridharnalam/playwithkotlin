package com.ideahamster.playkotlin

fun main(args: Array<String>) {

open class Animal(val name: String) {
    override fun toString(): String {
        return name
    }
}
class Dog(name: String): Animal(name)
class Cat(name: String): Animal(name)

val animals: List<Animal> = listOf(Cat("Scratchy"), Dog("Poochie"))
val cats = animals.filterIsInstance(Cat::class.java)

println(cats) // [Scratchy]

}