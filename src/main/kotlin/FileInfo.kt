import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.rxkotlin.toObservable
import java.io.File

data class FileInfo(
        val file: File,
        var bytes: Long = 0,
        var filesCount: Long = 1
)

fun fileToFileInfo(file: File): Observable<FileInfo> {
    if (file.isFile) {
        return Observable.just(FileInfo(file, file.length()))
    } else if (file.isDirectory) {
        return file.listFiles().toObservable()
                .flatMap { fileToFileInfo(it) }
                .reduce(FileInfo(file), { acc, subFileInfo ->
                    acc.bytes += subFileInfo.bytes
                    acc.filesCount += subFileInfo.filesCount
                    acc
                }).toObservable()

    }
    return Observable.error(IllegalArgumentException("$file is neither a file or a directory"))
}


fun sampleFunction(): Observable<Int> {
    val ints = arrayListOf<Int>(1, 2, 3)
    ints.map { singleInt -> singleInt * 2 }
    val myObservableSample = arrayListOf(1, 2, 3).toObservable() // is different from Observable.fromArray(arrayOf(1,2,3)
            .flatMap { just(it * 2) }
    return myObservableSample
}

fun main(args: Array<String>) {
    Observable.just(File("src/main/kotlin"))
            .flatMap { fileToFileInfo(it) }
            .subscribe(
                    { fileInfo -> println(fileInfo) },
                    { throwable -> throwable.printStackTrace() },
                    { println("On complete") }
            )
//    println("Printing directory")
//    val path = System.getProperty("user.dir")
//    println("working directory: $path")
}