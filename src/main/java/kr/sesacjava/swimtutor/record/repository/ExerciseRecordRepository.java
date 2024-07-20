package kr.sesacjava.swimtutor.record.repository;

import kr.sesacjava.swimtutor.record.entity.ExerciseRecord;
import kr.sesacjava.swimtutor.record.entity.ExerciseRecordId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, ExerciseRecordId> {
//
//    @Query(value = "select r " +
//            "from exercise_record r " +
//            "where r.oauthLoginId = :email and r.oauthLoginPlatform = :platform")
//    List<ExerciseRecord> findByEmailAndPlatform(@Param("email") String email, @Param("platform") String platform);
}
