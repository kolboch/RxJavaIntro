import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import java.io.File

data class FileInfo(
        val file: File,
        var bytes: Long = 0,
        var filesCount: Long = 1
)

private var DEBUG = true

fun fileToFileInfo(file: File): Observable<FileInfo> {
    if (file.isFile) {
        return Observable.just(FileInfo(file, file.length())).print({ "file: ${it.file}" })
    } else if (file.isDirectory) {
        return Observable.defer {
            file.listFiles().toObservable()
                    .flatMap { fileToFileInfo(it) }
                    .reduce(FileInfo(file), { acc, subFileInfo ->
                        acc.bytes += subFileInfo.bytes
                        acc.filesCount += subFileInfo.filesCount
                        acc
                    }).toObservable()
                    .print({ "reduced ${it.file}" })
        }.subscribeOn(Schedulers.io())
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

fun <T> Observable<T>.print(message: (T) -> String = { "" }): Observable<T> {
    return if (DEBUG)
        this.map {
            println("[${Thread.currentThread().name}] ${message(it)}")
            it
        }
    else {
        this
    }
}

fun main(args: Array<String>) {
    Observable.just(File(".idea"))
            .observeOn(Schedulers.io())
            .flatMap { fileToFileInfo(it) }
            .subscribe(
                    { fileInfo -> println("[${Thread.currentThread().name}] $fileInfo") },
                    { throwable -> throwable.printStackTrace() },
                    { println("On complete") }
            )
//    println("Printing directory")
//    val path = System.getProperty("user.dir")
//    println("working directory: $path")
    Thread.sleep(2000)
}