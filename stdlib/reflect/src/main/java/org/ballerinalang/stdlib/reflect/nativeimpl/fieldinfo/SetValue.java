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
package org.ballerinalang.stdlib.reflect.nativeimpl.fieldinfo;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.bre.bvm.CPU;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import static org.ballerinalang.stdlib.reflect.ReflectConstants.FIELD_INFO_FIELD_TYPE;
import static org.ballerinalang.stdlib.reflect.ReflectConstants.FIELD_INFO_IDENTIFIER;
import static org.ballerinalang.stdlib.reflect.ReflectConstants.FIELD_INFO_OBJECT_TYPE;

/**
 * Set the value for the field in the specified instance of the object.
 *
 * @since 0.981.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "reflect",
        functionName = "setValue",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "FieldInfo", structPackage = "ballerina/reflect"),
        args = {@Argument(name = "instance", type = TypeKind.OBJECT),
                @Argument(name = "value", type = TypeKind.ANY)},
        isPublic = true
)
public class SetValue extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BMap fieldInfo = (BMap) context.getRefArgument(0);

        if ((!(context.getRefArgument(1) instanceof BMap))
                || context.getRefArgument(1).getType()
                != ((BTypeDescValue) fieldInfo.get(FIELD_INFO_OBJECT_TYPE)).value()) {
            context.setReturnValues(BLangVMErrors.createError(context, "invalid argument, expected object of type: "
                    + fieldInfo.get(FIELD_INFO_OBJECT_TYPE) + ", found: " + context.getRefArgument(1).getType()));
            return;
        }

        BMap objInstance = (BMap) context.getRefArgument(1);
        BValue value = context.getRefArgument(2);

        if (!CPU.checkCast(value, ((BTypeDescValue) fieldInfo.get(FIELD_INFO_FIELD_TYPE)).value())) {
            context.setReturnValues(BLangVMErrors.createError(context, "invalid argument, value of type: "
                    + value.getType() + " cannot be assigned to a field of type: "
                    + fieldInfo.get(FIELD_INFO_FIELD_TYPE)));
            return;
        }

        objInstance.getMap().put(fieldInfo.get(FIELD_INFO_IDENTIFIER).stringValue(), value);
        context.setReturnValues();
    }
}
