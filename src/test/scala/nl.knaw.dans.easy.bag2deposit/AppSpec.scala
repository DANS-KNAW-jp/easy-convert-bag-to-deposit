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
package nl.knaw.dans.easy.bag2deposit

import better.files.File
import nl.knaw.dans.easy.bag2deposit.Fixture.{ AppConfigSupport, BagIndexSupport, FileSystemSupport }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Success

class AppSpec extends AnyFlatSpec with Matchers with AppConfigSupport with FileSystemSupport with BagIndexSupport {
  "createSips" should "log all kind of io errors" in {
    val appConfig = mockedConfig(null)
    File("src/test/resources/bags/01").children.toArray.foreach { testBag =>
      testBag.copyTo(
        (testDir / "exports" / testBag.name).createDirectories()
      )
    }
    new EasyConvertBagToDespositApp(appConfig).addPropsToBags(
      (testDir / "exports").children,
      IdType.DOI,
      None,
      DepositPropertiesFactory(appConfig)
    ) shouldBe Success("See logging")
    testDir / "exports" / "04e638eb-3af1-44fb-985d-36af12fccb2d" / "deposit.properties" should exist
  }
  it should "move the completed deposit" in {
    // valid test bag
    val uuid = "04e638eb-3af1-44fb-985d-36af12fccb2d"

    File("src/test/resources/bags/01/" + uuid).copyTo(
      (testDir / "exports" / uuid).createDirectories()
    )
    val appConfig = mockedConfig(null)

    new EasyConvertBagToDespositApp(appConfig).addPropsToBags(
      (testDir / "exports").children,
      IdType.DOI,
      Some((testDir / "ingest-dir").createDirectories()),
      DepositPropertiesFactory(appConfig)
    ) shouldBe Success("See logging")

    (testDir / "exports").children shouldBe empty
    testDir / "ingest-dir" / uuid / "deposit.properties" should exist
    // TODO check manifest and user in bag-info.txt
  }
}
