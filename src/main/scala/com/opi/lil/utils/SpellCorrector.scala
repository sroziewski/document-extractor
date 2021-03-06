package com.opi.lil.utils

import java.nio.charset.{CodingErrorAction}

import util.matching.Regex.MatchIterator
import scala.io.Codec

/**
 * Created by roziewski on 2015-10-22.
 */
class SpellCorrector(corpusDir: String) {

  val alphabet = Array("q","w","e","r","t","y","u","i","o","p","a","s","d","f","g","h","j","k","l","z","x","c","v","b","n","m","ą","ż","ź","ć","ń","ó","ł","ę","ś")
  def train(features : MatchIterator) = (Map[String, Int]() /: features)((m, f) => m + ((f, m.getOrElse(f, 0) + 1)))
  def words(text : String) = ("[%s]+" format alphabet.mkString).r.findAllIn(text.toLowerCase)
  val decoder = Codec.UTF8.decoder.onMalformedInput(CodingErrorAction.IGNORE)
  val dict = train(words(scala.io.Source.fromFile(corpusDir)(decoder).mkString))
  val invMap = scala.io.Source.fromFile(corpusDir)(decoder).getLines.map{l => l->1}.toMap
  val i =1

  def edits(s : Seq[(String, String)]) = (for((a,b) <- s; if b.length > 0) yield a + b.substring(1)) ++
    (for((a,b) <- s; if b.length > 1) yield a + b(1) + b(0) + b.substring(2)) ++
    (for((a,b) <- s; c <- alphabet if b.length > 0) yield a + c + b.substring(1)) ++
    (for((a,b) <- s; c <- alphabet) yield a + c + b)

  def edits1(word : String) = edits(for(i <- 0 to word.length) yield (word take i, word drop i))
  def edits2(word : String) = for(e1 <- edits1(word); e2 <-edits1(e1)) yield e2
  def known(words : Seq[String]) = for(w <- words; found <- dict.get(w)) yield w
  def or[T](candidates : Seq[T], other : => Seq[T]) = if(candidates.isEmpty) other else candidates

  def candidates(word: String) = or(known(List(word)), or(known(edits1(word)), known(edits2(word))))

  def correctPolish(word : String) = ((-1, word) /: candidates(word))(
  (max, word) => if(dict(word) > max._1) (dict(word), word) else max)._2
}


