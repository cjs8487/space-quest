package com.deepwelldevelopment.spacequest.engine;

import java.util.List;

import com.deepwelldevelopment.spacequest.engine.model.MaterialData;
import com.deepwelldevelopment.spacequest.engine.model.ModelData;

public record InitData(List<ModelData> models, List<MaterialData> materials) {
}