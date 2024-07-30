package kr.sesacjava.swimtutor.routine.service;

import kr.sesacjava.swimtutor.routine.dto.RequestRoutineDTO;
import kr.sesacjava.swimtutor.routine.dto.ResponseRoutineDTO;
import kr.sesacjava.swimtutor.routine.dto.TrainingForRoutineDTO;
import kr.sesacjava.swimtutor.routine.entity.Routine;
import kr.sesacjava.swimtutor.routine.entity.id.RoutineId;
import kr.sesacjava.swimtutor.security.dto.UserInfo;

import java.util.List;

public interface RoutineService {

    // 유저별 루틴 목록 조회
    List<ResponseRoutineDTO> getSeveralRoutines(UserInfo userInfo);

    // 루틴 목록
    List<ResponseRoutineDTO> getAllRoutines();

    // 루틴 상세
    List<TrainingForRoutineDTO> getRoutineDetail(RoutineId routineId);

    // 루틴 저장
    Routine saveRoutine(UserInfo userInfo, RequestRoutineDTO requestRoutineDTO);

    // 루틴 삭제
    Routine deleteRoutine(RoutineId routineId);
}
