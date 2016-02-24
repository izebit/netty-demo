package ru.izebit.services;


public interface SuperService {

    /**
     * @param level – номер уровня пирамиды.
     * @param index – номер элемента внутри уровня.
     * @return если элемент с указанным номером на данном уровне отсутствует то null
     * иначе значение массы,
     * которую «несет» на своей верхней (горизонтальной) грани
     * соответствующий элемент на соответствующем уровне.
     */
    Double getHumanEdgeWeight(int level, int index) throws Exception;
}
