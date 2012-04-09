object GraphicalEditor {

  type Colour = Char
  type Picture = List[List[Colour]]

  abstract sealed case class Command()
  case class I(m:Int, n:Int) extends Command
  case class C() extends Command
  case class L(x:Int, y:Int, c:Colour) extends Command
  case class V(x:Int, y1:Int, y2:Int, c:Colour) extends Command
  case class H(x1:Int, x2:Int, y1:Int, c:Colour) extends Command
  case class K(x1:Int, x2:Int, y1:Int, y2:Int, c:Colour) extends Command
  case class F(x:Int, y:Int, c:Colour) extends Command
  case class S(name:String) extends Command
  case class X() extends Command


  def white : Colour = 'O'
  def zeroSizePicture : Picture = List(List())
  def content(p: Picture) : String = p.transpose.map(_.mkString).mkString("\n");

  def processAll(cmds:List[Command]) : Picture =
    cmds.foldLeft(zeroSizePicture)(processSingle(_, _))

  def processSingle(p:Picture, c:Command) : Picture =
    c match {
      case I(m, n)                => List.fill(m, n)(white)
      case C()                    => p.map(_.map(_ => white))
      case L(x, y, nc)            => updateColour (p, ifOnPoint(x, y), nc)
      case V(x, y1, y2, nc)       => updateColour (p, ifOnVerticalLine(x, y1, y2), nc)
      case H(x1, x2, y, nc)       => updateColour (p, ifOnHorizontalLine(x1, x2, y), nc)
      case K(x1, x2, y1, y2, nc)  => updateColour (p, ifInRect(x1, x2, y1, y2), nc)
      case F(x, y, nc)            => updateColour (p, ifToBeFilled(p, x, y), nc)
      case S(n)                   => println(n); println(content(p)); p
      case X()                    => zeroSizePicture
    }

  def ifOnPoint(x:Int, y:Int) = (r:Int, c:Int) => (x == r) && (y == c)
  def ifOnVerticalLine(x:Int, y1:Int, y2:Int) = (r:Int, c:Int) => (x == r) && (y1 <= c) && (c <= y2)
  def ifOnHorizontalLine(x1:Int, x2:Int, y:Int) = (r:Int, c:Int) => (x1 <= r) && (r <= x2) && (y == c)
  def ifInRect(x1:Int, x2:Int, y1:Int, y2:Int) = (r:Int, c:Int) => (x1 <= r) && (r <= x2) && (y1 <= c) && (c <= y2)
  def ifToBeFilled(p:Picture, x:Int, y:Int) = (r:Int, c:Int) => calculateConnected(p, x, y).contains(r + "|" + c)

  def calculateConnected(p:Picture, x:Int, y:Int) = connecteds(p, Map.empty[String,Boolean], (x, y))

  def connecteds(p:Picture, m:Map[String, Boolean], t: (Int, Int)) : Map[String, Boolean] =
    t match { case (x:Int, y:Int) =>
      if (outsideBoundaries(p, x, y) || m.contains(x + "|" + y))
        m
      else {
        val nm : Map[String, Boolean] = m + (x + "|" + y -> true)
        List(
          up(x, y),
          right(x,y),
          down(x,y),
          left(x,y)).foldLeft(nm)(connecteds(p, _, _))
      }
  }

  def up(x:Int, y:Int) : (Int, Int)  = (x-1, y)
  def right(x:Int, y:Int) : (Int, Int) = (x, y+1)
  def down(x:Int, y:Int) : (Int, Int) = (x+1, y)
  def left(x:Int, y:Int) : (Int, Int) = (x, y-1)

  def outsideBoundaries(p:Picture, x:Int, y:Int) =
    p.isEmpty || x <= 0 || x > p.length || y <= 0 || y > p.head.length

  def updateColour(p:Picture, f:(Int, Int) => Boolean, newColour:Colour): Picture = {
    p.zipWithIndex.map {
      case (rs, r) => rs.zipWithIndex.map {
        case (col, c) => if (f(r + 1, c + 1)) newColour else col
      }}
  }

  def main(args: Array[String]) {
    val commands = List(
      I(5, 6),
      L(2, 3, 'A'),
      S("one.bmp"),
      F(3, 3, 'J'),
      V(2, 3, 4, 'W'),
      H(3, 4, 2, 'Z'),
      S("two.bmp"),
      X()
    )
    processAll(commands)
  }
}
