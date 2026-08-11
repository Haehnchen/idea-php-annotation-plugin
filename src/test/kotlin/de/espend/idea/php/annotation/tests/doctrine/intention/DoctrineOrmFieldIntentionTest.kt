package de.espend.idea.php.annotation.tests.doctrine.intention

import com.jetbrains.php.lang.PhpFileType
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.intention.DoctrineOrmFieldIntention
 */
class DoctrineOrmFieldIntentionTest : AnnotationLightCodeInsightFixtureTestCase() {
    public override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String {
        return "src/test/kotlin/de/espend/idea/php/annotation/tests/doctrine/intention/fixtures"
    }

    fun testThatAddDoctrineColumnIsAvailable() {
        assertIntentionIsAvailable(
            PhpFileType.INSTANCE,
            """
                <?php

                class Foobar
                {
                   public ${'$'}i<caret>d;}
            """.trimIndent(),
            "Add Doctrine column",
        )
    }

    fun testThatAddDoctrineColumnIsAvailableIsInvokedWithResult() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php

                /**
                 * @\Doctrine\ORM\Mapping\Entity
                 */
                class Existing {}

                class Foobar
                {
                   public ${'$'}i<caret>d;
                }
            """.trimIndent(),
        )

        val action = myFixture.findSingleIntention("Add Doctrine column")
        myFixture.launchAction(action)

        myFixture.checkResult(
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @\Doctrine\ORM\Mapping\Entity
                 */
                class Existing {}

                class Foobar
                {
                    /**
                     * @ORM\Id
                     * @ORM\GeneratedValue(strategy="AUTO")
                     * @ORM\Column(type="integer")
                     */public ${'$'}id;
                }
            """.trimIndent(),
        )
    }

    fun testThatAddDoctrineColumnIsAvailableIsInvokedWithResultForNullable() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php

                /**
                 * @\Doctrine\ORM\Mapping\Entity
                 */
                class Existing {}

                class Foobar
                {
                   public ?int ${'$'}foo<caret>bar;
                }
            """.trimIndent(),
        )

        val action = myFixture.findSingleIntention("Add Doctrine column")
        myFixture.launchAction(action)

        myFixture.checkResult(
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @\Doctrine\ORM\Mapping\Entity
                 */
                class Existing {}

                class Foobar
                {
                    /**
                     * @ORM\Column(type="integer", nullable=true)
                     */public ?int ${'$'}foobar;
                }
            """.trimIndent(),
        )
    }

    fun testThatAddDoctrineColumnIsAvailableIsInvokedWithResultForAttributes() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php

                #[Foo]
                class Foobar
                {
                   public ${'$'}i<caret>d;
                }
            """.trimIndent(),
        )

        val action = myFixture.findSingleIntention("Add Doctrine column")
        myFixture.launchAction(action)

        myFixture.checkResult(
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                #[Foo]
                class Foobar
                {
                    #[ORM\Id]
                    #[ORM\GeneratedValue(strategy: 'AUTO')]
                    #[ORM\Column(type: 'integer')]
                    public ${'$'}id;
                }
            """.trimIndent(),
        )
    }

    fun testThatAddDoctrineColumnIsAvailableIsInvokedWithResultForAttributesWithNullable() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                #[Foo]
                class Foobar
                {
                    #[ORM\Column(type: 'string')]
                    public ${'$'}foobar;
                }
            """.trimIndent(),
        )
    }

    fun testThatAddDoctrineColumnIsAvailableIsInvokedWithResultForAttributesForExisting() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php

                class Foobar
                {
                    #[ORM\GeneratedValue(strategy: 'AUTO')]
                    public ${'$'}i<caret>d;
                }
            """.trimIndent(),
        )

        val action = myFixture.findSingleIntention("Add Doctrine column")
        myFixture.launchAction(action)

        myFixture.checkResult(
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                class Foobar
                {
                    #[ORM\Id]
                    #[ORM\Column(type: 'integer')]
                    #[ORM\GeneratedValue(strategy: 'AUTO')]
                    public ${'$'}id;
                }
            """.trimIndent(),
        )
    }

    fun testThatAddDoctrineColumnIsAvailableIsInvokedWithResultForAttributesForExistingA() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                class Foobar
                {
                    public ${'$'}i<caret>d;

                    /**
                    * @ORM\Column()
                    */
                    public ${'$'}createdAt;
                }
            """.trimIndent(),
        )

        val action = myFixture.findSingleIntention("Add Doctrine column")
        myFixture.launchAction(action)

        myFixture.checkResult(
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                class Foobar
                {
                    /**
                     * @ORM\Id
                     * @ORM\GeneratedValue(strategy="AUTO")
                     * @ORM\Column(type="integer")
                     */
                    public ${'$'}id;

                    /**
                    * @ORM\Column()
                    */
                    public ${'$'}createdAt;
                }
            """.trimIndent(),
        )
    }
}
