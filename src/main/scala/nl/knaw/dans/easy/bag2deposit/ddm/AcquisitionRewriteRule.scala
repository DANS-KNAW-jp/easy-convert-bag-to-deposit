/**
 * Copyright (C) 2020 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.bag2deposit.ddm

import better.files.File
import nl.knaw.dans.easy.bag2deposit.ddm.AcquisitionRewriteRule.rowsWithRegexp
import nl.knaw.dans.easy.bag2deposit.parseCsv
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.apache.commons.csv.{ CSVFormat, CSVRecord }

import java.util.UUID
import scala.collection.generic.FilterMonadic
import scala.xml.transform.RewriteRule
import scala.xml.{ Elem, Node }

case class AcquisitionRewriteRule(cfgDir: File) extends RewriteRule with DebugEnhancedLogging {

  val acquisitionMap: Map[String, UUID] = {
    rowsWithRegexp(cfgDir)
      .map(r =>
        r.get("regexp") -> UUID.fromString(r.get("uuid"))
      ).toMap
  }

  override def transform(n: Node): Seq[Node] = {
    if (n.label != "title" && n.label != "alternative") n
    else {
      val lowerCaseTitle = n.text.trim.toLowerCase
      acquisitionMap
        .withFilter { case (regexp, _) =>
          lowerCaseTitle.matches(regexp)
        }
        .map { case (_, uuid) => toMethod(n.text)(uuid) }
        .toSeq :+ n
    }
  }

  private def toMethod(titleValue: String)(uuid: UUID): Elem = {
    <ddm:acquisitionMethod
      schemeURI="https://data.cultureelerfgoed.nl/term/id/abr/554ca1ec-3ed8-42d3-ae4b-47bcb848b238"
      valueURI={ s"https://data.cultureelerfgoed.nl/term/id/abr/$uuid" }
      subjectScheme="ABR verwervingswijzen"
    >{ titleValue }</ddm:acquisitionMethod>
  }
}
object AcquisitionRewriteRule {
  private val csvFormat = CSVFormat.RFC4180
    .withHeader("hiddenLabel", "uuid", "prefLabel", "regexp")
    .withDelimiter(',')
    .withRecordSeparator('\n')
    .withSkipHeaderRecord(true)

  private val length = csvFormat.getHeader.length

  def rowsWithRegexp(cfgDir: File): FilterMonadic[CSVRecord, Iterable[CSVRecord]] = {
    parseCsv(cfgDir / "acquisitionMethods.csv", 0, AcquisitionRewriteRule.csvFormat)
      .withFilter(_.size() == length)
  }
}
