/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.provider.pojo.reference;

import java.lang.reflect.Field;

import org.springframework.context.ApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringValueResolver;

import io.servicecomb.core.provider.CseBeanPostProcessor.ConsumerFieldProcessor;
import io.servicecomb.provider.pojo.RpcReference;

@Component
public class RpcReferenceProcessor implements ConsumerFieldProcessor, EmbeddedValueResolverAware {
  private StringValueResolver resolver;

  @Override
  public void processConsumerField(ApplicationContext applicationContext, Object bean, Field field) {
    RpcReference reference = field.getAnnotation(RpcReference.class);
    if (reference == null) {
      return;
    }

    handleReferenceField(bean, field, reference);
  }

  @Override
  public void setEmbeddedValueResolver(StringValueResolver resolver) {
    this.resolver = resolver;
  }

  private void handleReferenceField(Object obj, Field field,
      RpcReference reference) {
    String microserviceName = reference.microserviceName();
    microserviceName = resolver.resolveStringValue(microserviceName);

    PojoReferenceMeta pojoReference = new PojoReferenceMeta();
    pojoReference.setMicroserviceName(microserviceName);
    pojoReference.setSchemaId(reference.schemaId());
    pojoReference.setConsumerIntf(field.getType());

    pojoReference.afterPropertiesSet();

    ReflectionUtils.makeAccessible(field);
    ReflectionUtils.setField(field, obj, pojoReference.getProxy());
  }
}
