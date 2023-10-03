/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
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
package io.vertx.micrometer.impl;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.DatagramSocketMetrics;
import io.vertx.micrometer.Label;
import io.vertx.micrometer.MetricsDomain;
import io.vertx.micrometer.MetricsNaming;
import io.vertx.micrometer.impl.meters.LongGauges;

import java.util.EnumSet;

import static io.vertx.micrometer.Label.CLASS_NAME;
import static io.vertx.micrometer.Label.LOCAL;

/**
 * @author Joel Takvorian
 */
class VertxDatagramSocketMetrics extends AbstractMetrics implements DatagramSocketMetrics {

  private volatile DistributionSummary bytesWritten;
  private volatile DistributionSummary bytesRead;

  VertxDatagramSocketMetrics(MeterRegistry registry, MetricsNaming names, LongGauges longGauges, EnumSet<Label> enabledLabels) {
    super(registry, names, MetricsDomain.DATAGRAM_SOCKET, longGauges, enabledLabels);
  }

  @Override
  public void listening(String localName, SocketAddress localAddress) {
    bytesRead = distributionSummary(names.getDatagramBytesRead())
      .description("Total number of datagram bytes received")
      .tags(toTags(LOCAL, Labels::address, localAddress, localName))
      .register(registry);
  }

  @Override
  public void bytesRead(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    if (bytesRead != null) {
      bytesRead.record(numberOfBytes);
    }
  }

  @Override
  public void bytesWritten(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    if (bytesWritten == null) {
      bytesWritten = distributionSummary(names.getDatagramBytesWritten())
        .description("Total number of datagram bytes sent")
        .register(registry);
    }
    bytesWritten.record(numberOfBytes);
  }

  @Override
  public void exceptionOccurred(Void socketMetric, SocketAddress remoteAddress, Throwable t) {
    counter(names.getDatagramErrorCount())
      .description("Total number of datagram errors")
      .tags(toTags(CLASS_NAME, Class::getSimpleName, t.getClass()))
      .register(registry)
      .increment();
  }
}
