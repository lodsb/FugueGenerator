# FugueGenerator - Collaborative Melody Composition using a Generative Affect Model

This is the original source code for the prototypical rhythm sequencing application in the 
publication:

Klügel, Niklas and Hagerer, Gerhard and Groh, Georg. "FugueGenerator - Collaborative Melody Composition Based on a Generative
Approach for Conveying Emotion in Music"

Abstract:
This paper exemplifies an approach for generative music
software. Therefore new operational principles are used,
i.e. drawing melody contours and changing their emotional
expression by making use of valence and arousal. Known
connections between music, emotions and algorithms out
of existing literature are used to deliver a software experience
that augments the skills of individuals to make music
according to the emotions they want. A user-study lateron
shows the soundness of the implementation in this regard.
A comprehensive analysis of in-game statistics makes it
possible to measure the music produced by testers so that
connections between valence, arousal, melody properties
and contours and emotions will be depicted. In addition,
temporal sequences of reaction patterns between music making
individuals during their creative interaction are made
visible. A questionnaire filled out by the testers puts the
results on a solid foundation and shows that the incorporated
methods are appreciated by the users to apply emotions
musically as well as for being creative in a free and
joyful manner.


which can be found here: http://lodsb.org/fugue_generator_final.pdf , published in the proceedings of the ICMC2014

FugueGenerator is originally a tabletop application but the framework also allows use with mouse input apart from TUIO and native win7 touch.

Dependencies:
- reakt: https://github.com/lodsb/reakt
- UltraCom: https://github.com/lodsb/UltraCom

- checkout and build + sbt publish for each of these

- The real-time audio synthesis uses SuperCollider / ScalaCollider, therefore an additional SuperCollider installation is necessary.



