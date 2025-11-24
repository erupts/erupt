package xyz.erupt.bi.cube.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import xyz.erupt.bi.cube.annotation.Cube;
import xyz.erupt.bi.cube.annotation.Dimension;
import xyz.erupt.bi.cube.annotation.Explore;
import xyz.erupt.bi.cube.annotation.Measure;
import xyz.erupt.bi.cube.pojo.core.CubeModel;
import xyz.erupt.bi.cube.pojo.core.DimensionModel;
import xyz.erupt.bi.cube.pojo.core.MeasureModel;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Order(100)
@Service
@Slf4j
public class CubeCoreService implements ApplicationRunner {

    private static final Map<String, CubeModel> CUBE_MAP = new LinkedCaseInsensitiveMap<>();

    private static final List<CubeModel> CUBES = new ArrayList<>();

    public static CubeModel get(String name) {
        return CUBE_MAP.get(name);
    }

    public static List<CubeModel> getCubes() {
        return CUBES;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(), new TypeFilter[]{
                new AnnotationTypeFilter(Cube.class)
        }, clazz -> {
            CubeModel cubeModel = CubeCoreService.initEruptCube(clazz);
            CUBES.add(cubeModel);
            CUBE_MAP.put(clazz.getSimpleName(), cubeModel);
        });
    }

    private static CubeModel initEruptCube(Class<?> clazz) {
        CubeModel cubeModel = new CubeModel();
        cubeModel.setClazz(clazz);
        cubeModel.setCube(clazz.getAnnotation(Cube.class));
        for (Explore explore : cubeModel.getCube().explores()) {
            cubeModel.getExploreMap().put(explore.name(), explore);
        }
        for (Field field : clazz.getDeclaredFields()) {
            Dimension dimension = field.getAnnotation(Dimension.class);
            if (null != dimension) {
                DimensionModel dimensionModel = new DimensionModel();
                dimensionModel.setDimension(dimension);
                dimensionModel.setField(field);
                cubeModel.getDimensionMap().put(field.getName(), dimensionModel);
            }
            Measure measure = field.getAnnotation(Measure.class);
            if (null != measure) {
                MeasureModel measureModel = new MeasureModel();
                measureModel.setMeasure(measure);
                measureModel.setField(field);
                cubeModel.getMeasureMap().put(field.getName(), measureModel);
            }
        }
        return cubeModel;
    }


}
