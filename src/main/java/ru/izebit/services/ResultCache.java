package ru.izebit.services;

import java.util.Optional;

/**
 * интерфейс кэша вычисленных значений
 */
public interface ResultCache<T> {

    /**
     * @param level уровень пирамиды
     * @param index номер элемента
     * @return получение ранее вычисленного значения для данных параметров,
     * если в кэше нет данных Optional.empty()
     */
    Optional<T> getCachedResult(int level, int index);

    /**
     * кэширование результата
     *
     * @param level  уровень пирамиды
     * @param index  номер элемента
     * @param result вычисленное значение
     * @return true если значение отсутствовало, иначе false
     */
    boolean putCachedResult(int level, int index, T result);
}
