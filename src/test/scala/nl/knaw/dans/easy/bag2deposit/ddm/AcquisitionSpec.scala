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
import nl.knaw.dans.easy.bag2deposit.Fixture.FileSystemSupport
import nl.knaw.dans.easy.bag2deposit.ddm.AcquisitionRewriteRule.rowsWithRegexp
import nl.knaw.dans.easy.bag2deposit.parseCsv
import org.apache.commons.csv.CSVFormat
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AcquisitionSpec extends AnyFlatSpec with FileSystemSupport with Matchers {
  private val cfgDir: File = File("src/main/assembly/dist/cfg")

  "regular expressions" should "match common acquisition titles" in {
    def invert(titlesToLabels: Map[String, Seq[String]]) = {
      val tuples = titlesToLabels.toSeq.flatMap { case (title, labels) =>
        labels.map(_ -> title)
      }
      tuples.groupBy(_._1).map { case (label, tuples) =>
        label -> tuples.map(_._2).sortBy(identity)
      }
    }

    val format = CSVFormat.RFC4180.withHeader("verwervingswijzen", "titel")
    val titleToExpectedLabels = parseCsv(File("src/test/resources/commonAcquisitionTitles.csv"), 1, format)
      .map(r =>
        r.get("titel") -> r.get("verwervingswijzen").split("; ").toSeq
      ).toMap

    val regexpToLabel = rowsWithRegexp(cfgDir)
      .map(r =>
        r.get("regexp") -> r.get("hiddenLabel")
      ).toMap

    val titleToFoundLabels = titleToExpectedLabels.keys.map { title =>
      title -> regexpToLabel.filter { case (regexp, _) => title.trim.toLowerCase.matches(regexp) }.values.toSeq
    }.toMap

    val labelToFoundTitles = invert(titleToFoundLabels)
    val labelToExpectedTitles = invert(titleToExpectedLabels).filterKeys(!_.endsWith("?"))

    (testDir / "titles-expected-found.txt").write(
      titleToExpectedLabels.map { case (title, expected) =>
        val found = titleToFoundLabels(title)
        found.diff(expected).mkString("", ",", "\t") +
          expected.diff(found).mkString("", ",", "\t") +
          expected.mkString("", ",", "\t") +
          found.mkString("", ",", "\t") +
          title
      }.mkString("not-exp.\tnot-found\texpected\tfound\ttitle\n", "\n", "")
    )
    (testDir / "labels-expected-found.txt").write(
      labelToExpectedTitles.map { case (label, expectedTitles) =>
        val foundTitles = labelToFoundTitles(label)
        val notFound = expectedTitles.diff(foundTitles)
        val notExpected = foundTitles.diff(expectedTitles)
        val s1 = if (notFound.isEmpty) ""
                 else notFound.mkString(s"$label ---- not found ----\n\t", "\n\t", "")
        val s2 = if (notExpected.isEmpty) ""
                 else notExpected.mkString(s"\n$label ---- not expected ----\n\t", "\n\t", "")
        s1 + s2
      }.mkString("\n")
    )
    (testDir / "labels-expected-found.txt").lines.filter(_.endsWith("---- not expected ----")) shouldBe empty
    (testDir / "titles-expected-found.txt").lines.filter(_.endsWith("found[]")) shouldBe empty
    // TODO some minimum for proper matches?
  }
}
