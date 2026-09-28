package uz.askar.education.dictionaries;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DictionaryItemRepository extends JpaRepository<DictionaryItem, Long> {

    List<DictionaryItem> findByTypeOrderBySortOrderAscNameAsc(DictionaryType type);

    List<DictionaryItem> findByTypeAndActiveTrueOrderBySortOrderAscNameAsc(DictionaryType type);

    boolean existsByTypeAndCode(DictionaryType type, String code);

    List<DictionaryItem> findByIdIn(Collection<Long> ids);

    @Query("select coalesce(max(d.sortOrder), 0) from DictionaryItem d where d.type = :type")
    int maxSortOrder(@Param("type") DictionaryType type);
}
