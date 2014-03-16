/**
 * 
 */
package cz.mtheory.planet.enums;

import com.ardor3d.math.Vector3;

/**
 * M-theory project
 * 
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public enum CubeFace {
	ZERO(0, new CubeFaceZero()), ONE(1, new CubeFaceOne()), TWO(2,
			new CubeFaceTwo()), THREE(3, new CubeFaceThree()), FOUR(4,
			new CubeFaceFour()), FIVE(5, new CubeFaceFive());

	private final int index;
	private ICubeFace cubeFace;

	private CubeFace(final int index, final ICubeFace cubeFace) {
		this.index = index;
		this.cubeFace = cubeFace;
	}

	public int getIndex() {
		return index;
	}

	public void vectorTurn(Vector3 v) {
		cubeFace.doTurn(v);
	}

	public int findNeighborIndex(CubeFace destCubeFace, Quadrant srcQuadrant) {
		return cubeFace.neighborIndex(destCubeFace, srcQuadrant);
	}

	public int neighborSide(CubeFace destCubeFace) {
		return cubeFace.neighborSide(destCubeFace);
	}

	public static int size() {
		return 6;
	}

	public static CubeFace find(final int index) {
		switch (index) {
		case 0:
			return ZERO;
		case 1:
			return ONE;
		case 2:
			return TWO;
		case 3:
			return THREE;
		case 4:
			return FOUR;
		case 5:
			return FIVE;
		}
		throw new RuntimeException("Can not find CubeFace for index = " + index);
	}

	private static interface ICubeFace {
		public void doTurn(final Vector3 v);

		public int neighborIndex(final CubeFace destCubeFace,
				final Quadrant srcQuadrant);

		public int neighborSide(final CubeFace destCube);
	}

	private static class CubeFaceZero implements ICubeFace {
		@Override
		public final void doTurn(final Vector3 v) {
			// nothing
		}

		@Override
		public final int neighborIndex(final CubeFace destCubeFace,
				final Quadrant srcQuadrant) {
			return -1;
		}

		@Override
		public final int neighborSide(final CubeFace destCubeFace) {
			return -1;
		}

	}

	private static class CubeFaceOne implements ICubeFace {
		@Override
		public final void doTurn(final Vector3 v) {
			v.set(v.getZ(), v.getY(), -v.getX());
		}

		@SuppressWarnings("incomplete-switch")
		@Override
		public final int neighborIndex(final CubeFace destCubeFace,
				final Quadrant srcQuadrant) {
			switch (destCubeFace) {
			case FOUR:
				if (srcQuadrant.getIndex() == 2)
					return 1;
				return 3;
			case FIVE:
				if (srcQuadrant.getIndex() == 0)
					return 3;
				return 1;
			}
			return -1;
		}

		@SuppressWarnings("incomplete-switch")
		@Override
		public final int neighborSide(final CubeFace destCubeFace) {
			switch (destCubeFace) {
			case FOUR:
				return 1;
			case FIVE:
				return 3;
			}
			return -1;
		}
	}

	private static class CubeFaceTwo implements ICubeFace {
		@Override
		public final void doTurn(final Vector3 v) {
			v.set(-v.getX(), v.getY(), -v.getZ());
		}

		@SuppressWarnings("incomplete-switch")
		@Override
		public final int neighborIndex(final CubeFace destCubeFace,
				final Quadrant srcQuadrant) {
			switch (destCubeFace) {
			case FOUR:
				if (srcQuadrant.getIndex() == 2)
					return 3;
				return 2;
			case FIVE:
				if (srcQuadrant.getIndex() == 0)
					return 1;
				return 0;
			}
			return -1;
		}

		@SuppressWarnings("incomplete-switch")
		@Override
		public final int neighborSide(final CubeFace destCubeFace) {
			switch (destCubeFace) {
			case FOUR:
				return 1;
			case FIVE:
				return 3;
			}
			return -1;
		}

	}

	private static class CubeFaceThree implements ICubeFace {
		@Override
		public final void doTurn(final Vector3 v) {
			v.set(-v.getZ(), v.getY(), v.getX());
		}

		@SuppressWarnings("incomplete-switch")
		@Override
		public final int neighborIndex(final CubeFace destCubeFace,
				final Quadrant srcQuadrant) {
			switch (destCubeFace) {
			case FOUR:
				if (srcQuadrant.getIndex() == 2)
					return 2;
				return 0;
			case FIVE:
				if (srcQuadrant.getIndex() == 0)
					return 0;
				return 2;
			}
			return -1;
		}

		@SuppressWarnings("incomplete-switch")
		@Override
		public final int neighborSide(final CubeFace destCubeFace) {
			switch (destCubeFace) {
			case FOUR:
				return 1;
			case FIVE:
				return 3;
			}
			return -1;
		}

	}

	private static class CubeFaceFour implements ICubeFace {
		@Override
		public final void doTurn(final Vector3 v) {
			v.set(v.getX(), v.getZ(), -v.getY());
		}

		@SuppressWarnings("incomplete-switch")
		@Override
		public final int neighborIndex(final CubeFace destCubeFace,
				final Quadrant srcQuadrant) {
			switch (destCubeFace) {
			case ONE:
				if (srcQuadrant.getIndex() == 1)
					return 2;
				return 3;
			case TWO:
				if (srcQuadrant.getIndex() == 2)
					return 3;
				return 2;
			case THREE:
				if (srcQuadrant.getIndex() == 0)
					return 3;
				return 2;
			}
			return -1;
		}

		@SuppressWarnings("incomplete-switch")
		@Override
		public final int neighborSide(final CubeFace destCubeFace) {
			switch (destCubeFace) {
			case ONE:
				return 2;
			case TWO:
				return 1;
			case THREE:
				return 0;
			}
			return -1;
		}

	}

	private static class CubeFaceFive implements ICubeFace {
		@Override
		public final void doTurn(final Vector3 v) {
			v.set(v.getX(), -v.getZ(), v.getY());
		}

		@SuppressWarnings("incomplete-switch")
		@Override
		public final int neighborIndex(final CubeFace destCubeFace,
				final Quadrant srcQuadrant) {
			switch (destCubeFace) {
			case ONE:
				if (srcQuadrant.getIndex() == 1)
					return 1;
				return 0;
			case TWO:
				if (srcQuadrant.getIndex() == 0)
					return 1;
				return 0;
			case THREE:
				if (srcQuadrant.getIndex() == 0)
					return 0;
				return 1;
			}
			return -1;
		}

		@SuppressWarnings("incomplete-switch")
		@Override
		public final int neighborSide(final CubeFace destCubeFace) {
			switch (destCubeFace) {
			case ONE:
				return 2;
			case TWO:
				return 3;
			case THREE:
				return 0;
			}
			return -1;
		}

	}

}
