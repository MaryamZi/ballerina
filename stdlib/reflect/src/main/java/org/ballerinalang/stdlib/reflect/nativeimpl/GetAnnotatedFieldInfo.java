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
import org.ballerinalang.connector.impl.ConnectorSPIModelHelper;
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BObjectType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BUnionType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.StructureTypeInfo;

import java.util.Map;

/**
 * Get Function's Annotations.
 *
 * @since 0.981.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "reflect",
        functionName = "getAnnotatedFieldInfo",
        args = {@Argument(name = "annotatedObject", type = TypeKind.OBJECT),
                @Argument(name = "annotationDesc", type = TypeKind.TYPEDESC)},
        returnType = {@ReturnType(type = TypeKind.ARRAY)},
        isPublic = true
)
public class GetAnnotatedFieldInfo extends AbstractAnnotationReader {

    @Override
    public void execute(Context context) {
        BRefType annotatedObject = (BRefType) context.getRefArgument(0);
        BTypeDescValue annotationDesc = ((BTypeDescValue) context.getRefArgument(1));

        if (!(annotatedObject.getType() instanceof BObjectType)) {
            context.setReturnValues((BValue) null);
        }
        BObjectType objectType = (BObjectType) annotatedObject.getType();
        BMap annotationMap = ConnectorSPIModelHelper.getAnnotationVariable(objectType.getPackagePath(),
                                                                           context.getProgramFile());

        BType annotationType = annotationDesc.value();

        StructureTypeInfo annotatedFieldInfoStructInfo = context.getProgramFile().getPackageInfo(PKG_REFLECT)
                                                                .getStructInfo(ANNOTATED_FIELD_INFO);
        BRefValueArray annotationFieldArray = new BRefValueArray(annotatedFieldInfoStructInfo.getType());
        long index = 0;

        StructureTypeInfo fieldInfoStructInfo = context.getProgramFile().getPackageInfo(PKG_REFLECT)
                                                                .getStructInfo(FIELD_INFO);

        for (BField field : objectType.getFields()) {
            String key = objectType.getName() + DOT + field.fieldName;
            BMap<String, BValue> fieldAnnotations = (BMap<String, BValue>) annotationMap.get(key);
            for (Map.Entry<String, BValue> annotationDataEntry : fieldAnnotations.getMap().entrySet()) {
                if (annotationType.equals(annotationDataEntry.getValue().getType())) {
                    BMap<String, BValue> fieldInfo =
                            BLangVMStructs.createBStruct(fieldInfoStructInfo, field.fieldName,
                                                         new BTypeDescValue(field.fieldType),
                                                         field.fieldType.getTag() == TypeTags.UNION_TAG
                                                                 && ((BUnionType) field.fieldType).isNullable(),
                                                         new BTypeDescValue(objectType));
                    BMap<String, BValue> annotData = createAnnotationDataRecord(context, annotationDataEntry.getKey(),
                                                                (BMap<String, BValue>) annotationDataEntry.getValue());
                    BMap<String, BValue> annotatedFieldInfo =
                            BLangVMStructs.createBStruct(annotatedFieldInfoStructInfo, fieldInfo, annotData);
                    annotationFieldArray.add(index++, annotatedFieldInfo);
                }
            }
        }

        context.setReturnValues(annotationFieldArray);
    }
}
