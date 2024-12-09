import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*

suspend fun main() {

    println(
        parseSheet(
            "https://docs.google.com/spreadsheets/d/1f2yv6m0ujkkO7ht_9M0QjfGgnbxT9QemjCjt-dha86A/export?format=csv&gid=0"
        )
    )

}

suspend fun parseSheet(url: String): List<Question> {
    // Step 1: getData from url
    val csvData = fetchCsvData(url)

    // step 2: get all rows from csv data
    val rows: List<List<String>> = csvReader().readAll(csvData)

    // Step 3: Parse data to list<Questions>
    val questions = parseRowsToQuestions(rows)

    return questions
}

fun parseRowsToQuestions(rows: List<List<String>>): List<Question> {
    val questions = mutableListOf<Question>()
    rows.drop(1).forEach { row ->
            if (row[1] == Question.FreeText::class.java.name) {
                questions.add(
                    Question.FreeText(
                        question = row[0],
                        answer = row[3]
                    )
                )
            } else {
                questions.add(
                    Question.MCQ(
                        options = row[2].split("\n"),
                        question = row[0],
                        answer = row[3]
                    )
                )
            }

    }
    return questions
}

suspend fun fetchCsvData(url: String): String {
    val client = HttpClient(CIO)
    val csvData: String = client.get(url).body()
    client.close()
    return csvData
}



sealed class Question(
    open val question: String,
    open val answer: String
) {
    data class FreeText(
        override val question: String,
        override val answer: String
    ) : Question(question, answer)

    data class MCQ(
        val options: List<String>,
        override val question: String,
        override val answer: String
    ) : Question(question, answer)
}

