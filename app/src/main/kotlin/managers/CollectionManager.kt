package ru.qwuadrixx.app.managers

import ru.qwuadrixx.app.exception.NotFoundException
import ru.qwuadrixx.app.models.StudyGroup
import ru.qwuadrixx.app.utils.Console
import java.time.LocalDateTime
import java.util.*

/**
 * Класс, отвечающий за хранение данных и взаимодействие с ними
 * @author qwuadrixx
 */
class CollectionManager(
    override val collection: Vector<StudyGroup> = Vector(),
    private val console: Console,
    override var lastInitTime: LocalDateTime = LocalDateTime.now(),
    override var lastEditTime: LocalDateTime = LocalDateTime.now(),
    private val fileManager: IFileManager
) : ICollectionManager {

    init {
        collection.addAll(fileManager.readCollection() ?: emptyList())
    }

    /**
     * Метод, отвечающий за добавление элемента в коллекцию
     * @param studyGroup
     */
    override fun add(studyGroup: StudyGroup) {
        collection.add(studyGroup)
        lastEditTime = LocalDateTime.now()

        console.printLine("Элемент успешно добавлен")
    }

    /**
     * Метод, отвечающий за обновление элемента по id
     * @param id
     * @param studyGroup
     */
    override fun updateById(id: Int, studyGroup: StudyGroup) {
        val index = collection.indexOfFirst { it.id == id }

        if (index == -1) throw NotFoundException(String.format("Элемент с id: %d не найден", id))

        collection.removeAt(index)
        collection.add(index, studyGroup)
        lastEditTime = LocalDateTime.now()

        console.printLine("Элемент успешно изменен")
    }

    /**
     * Метод, отвечающий за удаление элемента по id
     * @param id
     */
    override fun removeById(id: Int) {
        val index = collection.indexOfFirst { it.id == id }

        if (index == -1) throw NotFoundException("Элемент с id: $id не найден")

        collection.removeAt(index)
        lastEditTime = LocalDateTime.now()

        console.printLine("Элемент успешно изменен")
    }

    /**
     * Метод, отвечающий за очистку коллекций
     */
    override fun clear() {
        collection.clear()
        lastEditTime = LocalDateTime.now()

        console.printLine("Коллекция успешно очищена")
    }

    /**
     * Метод, отвечающий за запись коллекции в файл
     */
    override fun saveCollectionToFile() {
        fileManager.writeCollection(collection)

        console.printLine("Коллекция успешно записана в файл")
    }

    override fun getCollection(): Collection<StudyGroup> = collection

    /**
     * Метод, отвечающий за добавления элемента на определенную позицию
     * @param index
     * @param studyGroup
     */
    override fun insertAt(index: Int, studyGroup: StudyGroup) {
        collection.add(index, studyGroup)
        lastEditTime = LocalDateTime.now()

        console.printLine("Элемент успешно добавлен на позицию $index")
    }

    /**
     * Метод, отвечающий за удаление последнего элемента в коллекции
     */
    override fun removeLast() {
        collection.removeLast()
        lastEditTime = LocalDateTime.now()

        console.printLine("Элемент на последней позиции удален")
    }

    /**
     * Метод, отвечающий за добавление элемента если его значение превышает значение наибольшего в коллекции
     * @param studyGroup
     */
    override fun addIfMax(studyGroup: StudyGroup) {
        val isMax = collection.stream().noneMatch { it >= studyGroup }

        if (isMax) {
            collection.add(studyGroup)
            console.printLine("Элемент успешно добавлен в коллекцию")
        } else {
            console.printLine("Элемент не был добавлен, так как не превышает значение наибольшего в коллекции")
        }
    }

    /**
     * Метод, отвечающий за подсчет средней оценки среди всех элементов
     * @return averageMark
     */
    override fun getAverageMarkFromAll(): Long {
        console.printLine("Среднее значение AverageMark:")
        return collection.stream()
            .mapToLong { it.averageMark }
            .average()
            .orElse(0.0)
            .toLong()
    }

    /**
     * Метод, отвечающий за подсчет количества групп со средней оценкой меньше заданной
     * @param averageMark
     * @return count
     */
    override fun countAverageMarkLessThen(averageMark: Long): Long {
        console.printLine("Количество элементов меньше $averageMark:")
        return collection.stream()
            .mapToLong { it.averageMark }
            .filter { it < averageMark }
            .count()
    }

    /**
     * Метод, отвечающий за подсчет количества групп со средней оценкой больше заданной
     * @param averageMark
     * @return count
     */
    override fun countAverageMarkGreaterThen(averageMark: Long): Long {
        console.printLine("Количество элементов больше $averageMark:")
        return collection.stream()
            .mapToLong { it.averageMark }
            .filter { it > averageMark }
            .count()
    }

    /**
     * Метод, отвечающий за сохранение коллекции при нарушении транзакции
     * @param collection
     */
    override fun saveSnapshot(collection: Collection<StudyGroup>) {
        this.collection.clear()
        this.collection.addAll(collection)
    }

    /**
     * Метод, отвечающий за сохранение копии коллекции для соблюдения транзакции
     * @return collection
     */
    override fun loadSnapshot(): Collection<StudyGroup> = collection.mapTo(Vector()) { it.copy() }
}