package kr.sesacjava.swimtutor.routine.service;

import kr.sesacjava.swimtutor.routine.dto.RequestRoutineDTO;
import kr.sesacjava.swimtutor.routine.dto.TrainingForRoutineDTO;
import kr.sesacjava.swimtutor.routine.entity.Routine;
import kr.sesacjava.swimtutor.routine.entity.Training;
import kr.sesacjava.swimtutor.routine.entity.TrainingForRoutine;
import kr.sesacjava.swimtutor.routine.repository.RoutineRepository;
import kr.sesacjava.swimtutor.routine.repository.TrainingForRoutineRepository;
import kr.sesacjava.swimtutor.routine.repository.TrainingRepository;
import kr.sesacjava.swimtutor.security.dto.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Log4j2
public class NewRoutineImpl implements NewRoutineService {

    private static final Logger LOG = LoggerFactory.getLogger(NewRoutineImpl.class);
    private static final List<String> SESSIONS = Arrays.asList("워밍업", "코어", "쿨다운");

    private TrainingRepository trainingRepo;
    private TrainingForRoutineRepository trainingForRoutineRepo;
    private RoutineImpl routineImpl;
    private RoutineRepository routineRepo;

    @Autowired
    public NewRoutineImpl(TrainingRepository trainingRepo, TrainingForRoutineRepository trainingForRoutineRepo, RoutineImpl routineImpl, RoutineRepository routineRepo) {
        this.trainingRepo = trainingRepo;
        this.trainingForRoutineRepo = trainingForRoutineRepo;
        this.routineImpl = routineImpl;
        this.routineRepo = routineRepo;
    }

    // 세션별 목표거리 계산
    public int calculateDistanceForSession(int baseDistance, double sessionPercentage, double variationPercentage) {
//        LOG.info("calculateDistanceForSession 호출");

        // 세션 목표거리의 하한값- 운동 난이도 하향 조절
        double minDistance = baseDistance * sessionPercentage * (1 - variationPercentage);
        // 세션 목표거리의 상한값 - 운동 난이도 상향 조절
        double maxDistance = baseDistance * sessionPercentage * (1 + variationPercentage);

        // 가장 가까운 50m 단위로 반올림
        return (int) (Math.round(maxDistance / 50) * 50);
    }

    // 세션별 선택 가능 훈련 목록 선정
    public List<Training> selectAvailableTrainings(int sessionTargetDistance, String selStrokes) {
//        LOG.info("selectAvailableTrainings 호출");

        List<Training> allTrainings = trainingRepo.findAll();
        List<Training> availableTrainings = new ArrayList<>();
        List<String> selStrokeList = Arrays.stream(selStrokes.split(","))
                .map(String::trim)
                .toList();

        for (Training training : allTrainings) {
            // 선택한 영법에 포함되는 훈련 중
            if (selStrokeList.contains(training.getStrokeName())
                    // 훈련 거리가 목표 거리보다 작거나 같고
                    && training.getDistance() * training.getSets() <= sessionTargetDistance
                    // 이미 확인한 훈련이 아니면
                    && !availableTrainings.contains(training)
            ) {
                // 선택 가능 훈련 목록에 추가
                availableTrainings.add(training);
            }
        }
        return availableTrainings;
    }

    // 세션별 훈련 선택
    public List<Training> selectTrainingsForSession(int sessionTargetDistance, List<Training> availableTrainings) {
//        LOG.info("selectTrainingsForSession 호출");

        List<Training> selectedTrainings = new ArrayList<>();

        // 목표거리가 0이 될 때까지 훈련을 선택
        for (int d = sessionTargetDistance; d > 0; ) {
            Random rand = new Random();
            int index = 1 + rand.nextInt(availableTrainings.size() - 1);

            // TODO: 세션별 난이도 고려하는 로직 구현
            // 선택 가능한 훈련 중에서 랜덤으로 선택
            Training selectedTraining = availableTrainings.get(index);

            // 선택된 훈련을 세션에 추가하고, 목표거리를 갱신
            selectedTrainings.add(selectedTraining);
            d = d - (selectedTraining.getDistance() * selectedTraining.getSets());

            // 선택된 훈련을 사용한 후, 다시 선택되지 않도록 목록에서 제거
            availableTrainings.remove(index);
        }
        return selectedTrainings;
    }

    // 루틴 상세 생성
    public List<TrainingForRoutineDTO> createTrainingsForRoutine(int targetDistance, String selStrokes) {
//        LOG.info("createRoutine 호출");

        List<TrainingForRoutineDTO> selectedTrainingsForRoutine = new ArrayList<>();
        // 세션별 목표거리 비율
        List<Double> sessionPercentages = Arrays.asList(0.3, 0.6, 0.1);
        // 목표거리 변동률
        double variationPercentage = 0.05;

        // 훈련 선택(세션 수만큼 반복)
        for (int i = 0; i < SESSIONS.size(); i++) {
            // 목표거리 계산
            int sessionTargetDistance = calculateDistanceForSession(targetDistance, sessionPercentages.get(i), variationPercentage);
            // 선택 가능한 훈련 목록
            List<Training> availableTrainingsForSession = selectAvailableTrainings(sessionTargetDistance, selStrokes);
            // 훈련 선택
            List<Training> selectedTrainings = selectTrainingsForSession(sessionTargetDistance, availableTrainingsForSession);

            // 선택된 훈련을 list에 추가
            for (Training training : selectedTrainings) {
                TrainingForRoutineDTO selectedTraining = TrainingForRoutineDTO.builder()
                        .session(SESSIONS.get(i))
                        .trainingId(training.getTrainingId())
                        .strokeName(training.getStrokeName())
                        .distance(training.getDistance())
                        .sets(training.getSets())
                        .build();
                selectedTrainingsForRoutine.add(selectedTraining);
            }
        }
        return selectedTrainingsForRoutine;
    }

    // 루틴 상세 저장
    public void saveTrainingsForRoutine(Routine routine, List<TrainingForRoutineDTO> trainingForRoutineDTOS) {
//        LOG.info("saveTrainingsForRoutine 호출");
        List<TrainingForRoutine> trainingsForRoutine = new ArrayList<>();
        for (TrainingForRoutineDTO trainingForRoutineDTO : trainingForRoutineDTOS) {
            TrainingForRoutine trainingForRoutine = TrainingForRoutine.builder()
                    .routineNo(routine.getRoutineNo())
                    .oauthLoginId(routine.getOauthLoginId())
                    .oauthLoginPlatform(routine.getOauthLoginPlatform())
                    .session(trainingForRoutineDTO.getSession())
                    .trainingId(trainingForRoutineDTO.getTrainingId())
                    .build();
            trainingsForRoutine.add(trainingForRoutine);
        }
        trainingForRoutineRepo.saveAll(trainingsForRoutine);
    }

    // 새 루틴 생성 및 저장
    public void saveNewRoutine(UserInfo userInfo, RequestRoutineDTO requestRoutineDTO) {
        LOG.info("saveNewRoutine 호출");
        LOG.info("Received userInfo: {}", userInfo.toString());

        int targetDistance = requestRoutineDTO.getTargetDistance();
        String selStrokes = requestRoutineDTO.getSelStrokes();

        // 루틴 저장
        routineImpl.saveRoutine(userInfo, requestRoutineDTO);
        int lastRoutineNo = routineRepo.findMaxRoutineNo(userInfo) == null ? 0 : routineRepo.findMaxRoutineNo(userInfo);
        LOG.info("lastRoutineNo: {}", lastRoutineNo);

        Routine routine = Routine.builder()
                .routineNo(lastRoutineNo)
                .oauthLoginId(userInfo.getEmail())
                .oauthLoginPlatform(userInfo.getPlatform())
                .routineName(requestRoutineDTO.getRoutineName())
                .poolLength(requestRoutineDTO.getPoolLength())
                .targetDistance(targetDistance)
                .selStrokes(selStrokes)
                .build();

        // 루틴에 포함될 훈련 목록 선택
        List<TrainingForRoutineDTO> trainingForRoutineDTOS = createTrainingsForRoutine(targetDistance, selStrokes);

        // 선택된 훈련을 DB에 저장
        saveTrainingsForRoutine(routine, trainingForRoutineDTOS);
    }
}
