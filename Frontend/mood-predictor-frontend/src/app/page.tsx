"use client";

import React from 'react';

import Autoplay from "embla-carousel-autoplay";
import { Button } from '@/components/ui/button';
import { Card, CardContent } from "@/components/ui/card";
import { Carousel, CarouselContent, CarouselItem } from '@/components/ui/carousel';

const Home = () => {
  const plugin = React.useRef(
    Autoplay({ delay: 2000, stopOnInteraction: true })
  )

  const moods = [
    { src: '/images/Happy.png', name: 'Happy' },
    { src: '/images/Flat.png', name: 'Flat' },
    { src: '/images/Sad.png', name: 'Sad' }
  ];

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 p-4">
      <div className="justify-center text-center text-white p-4">
        <h2 className="font-bold text-3xl text-gray-800 mb-6 text-center">Welcome to Mood Predictor!</h2>
        <Carousel plugins={[plugin.current]} className="w-full max-w-72 mx-auto mt-0" >
          <CarouselContent>
            {moods.map((moodItem, index) => (
              <CarouselItem key={index}> {}
                <div className="p-2 mt-8">
                  <Card className='bg-gradient-to-t from-white to-blue-600'>
                    <CardContent className="flex aspect-square items-center justify-center p-6">
                      <img src={moodItem.src} alt={`Mood ${index + 1}`} className='size-32 z-10' />
                    </CardContent>
                    <div className="text-blue-800 text-center text-2xl font-bold">
                      {moodItem.name}
                    </div>
                  </Card>
                </div>
              </CarouselItem>
            ))}
          </CarouselContent>
        </Carousel>
        <Button className="mt-6 font-bold text-2xl" onClick={() => window.location.href = '/login'}>
          Check In!    
        </Button>
      </div>
    </div>
  )
}

export default Home