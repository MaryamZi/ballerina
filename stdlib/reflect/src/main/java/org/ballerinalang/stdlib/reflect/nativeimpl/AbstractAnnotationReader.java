/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.reflect.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.impl.ConnectorSPIModelHelper;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;

import static org.ballerinalang.stdlib.reflect.ReflectConstants.PKG_REFLECT;

/**
 * Common logic for reading Global level annotation.
 *
 * @since 0.965.0
 */
abstract class AbstractAnnotationReader extends BlockingNativeCallableUnit {

    private static final String PKG_INTERNAL = "ballerina/internal";
    private static final String STRUCT_ANNOTATION = "annotationData";

    BValue getAnnotationValue(Context context, String pkgPath, String key) {
        final BMap bMap = ConnectorSPIModelHelper.getAnnotationVariable(pkgPath, context.getProgramFile());
        return createAnnotationStructArray(context, bMap.get(key));
    }

    private BRefValueArray createAnnotationStructArray(Context context, BValue map) {
        if (map == null || map.getType().getTag() != BTypes.typeMap.getTag()) {
            return null;
        }
        final PackageInfo packageInfo = context.getProgramFile().getPackageInfo(PKG_REFLECT);
        final StructureTypeInfo structInfo = packageInfo.getStructInfo(STRUCT_ANNOTATION);
        BRefValueArray annotationArray = new BRefValueArray(structInfo.getType());
        BMap<String, BValue> annotationMap = (BMap<String, BValue>) map;
        long index = 0;
        for (String key : annotationMap.keySet()) {
            annotationArray.add(index++, createAnnotationDataRecord(context, key,
                                                                    (BMap<String, BValue>) annotationMap.get(key)));
        }
        return annotationArray;
    }

    BMap<String, BValue> createAnnotationDataRecord(Context context, String key,
                                                              BMap<String, BValue> annotationValue) {
        PackageInfo packageInfo = context.getProgramFile().getPackageInfo(PKG_REFLECT);
        StructureTypeInfo structInfo = packageInfo.getStructInfo(STRUCT_ANNOTATION);

        String annotationQName = key.split("\\$")[0];
        String annotationName = annotationQName.substring(annotationQName.lastIndexOf(":") + 1);
        String pkgQName = annotationQName.substring(0, annotationQName.lastIndexOf(":"));
        String[] pkgQNameParts = pkgQName.split(":");
        String pkgVersion = pkgQNameParts.length > 1 ? pkgQNameParts[1] : "";
        return BLangVMStructs.createBStruct(structInfo, annotationName, pkgQNameParts[0], pkgVersion, annotationValue);
    }
}
