package com.github.xoaltie.cloudstorage.repository;

import com.github.xoaltie.cloudstorage.entity.Resource;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceRepository extends Repository<Resource, Long> {
    String FIND_DIRECTORY_DIRECT_CONTENTS_QUERY = """
            WITH RECURSIVE
                path_segments AS
                        (SELECT CAST(:pathSegments AS text[]) AS segments),
                path_nodes AS
                    (SELECT r.id, r.type, 1 AS depth
                     FROM resources r
                     WHERE r.user_id = :userId
                       AND r.parent_id IS NULL
                       AND LOWER(r.name) = (SELECT LOWER(ps.segments[1]) FROM path_segments ps)
                     UNION ALL
                     SELECT r.id, r.type, pn.depth + 1
                     FROM resources r
                              JOIN path_nodes pn ON r.parent_id = pn.id
                     WHERE r.user_id = :userId
                       AND LOWER(r.name) = (SELECT LOWER(ps.segments[pn.depth + 1]) FROM path_segments ps))
            SELECT r.id, r.name, r.type, r.size, r.parent_id, r.user_id
            FROM resources r
                     JOIN path_nodes pn ON r.parent_id = pn.id
                     CROSS JOIN path_segments ps
            WHERE r.user_id = :userId
              AND pn.depth = cardinality(ps.segments)
              AND pn.type = 'DIRECTORY'
            """;

    @NativeQuery(value = FIND_DIRECTORY_DIRECT_CONTENTS_QUERY)
    List<Resource> findDirectoryDirectContents(@Param("userId") Long userId, @Param("pathSegments") String[] pathSegments);

    List<Resource> findByUserIdAndParentIsNull(Long userId);
}
