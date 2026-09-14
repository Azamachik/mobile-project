fun main() {
    println(birthdayGreeting(age = 5))
    println(birthdayGreeting(age = 2))
}

fun birthdayGreeting(name: String = "Rover", age: Int): String {
    return "Happy Birthday, $name! You are now $age years old!"
}